package lifemaxxing.controller;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import jakarta.servlet.http.HttpSession;
import lifemaxxing.dto.FormError;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.dto.LoginForm;
import lifemaxxing.dto.RegisterForm;
import lifemaxxing.model.User;
import lifemaxxing.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public void setRoutes(JavalinConfig config) {
        config.routes.get("/", this::home);
        config.routes.get("/login", this::showLogin);
        config.routes.post("/login", this::login);
        config.routes.get("/register", this::showRegister);
        config.routes.post("/register", this::register);
        config.routes.get("/logout", this::logout);
    }

    public void home(Context ctx) throws DatabaseException {
        ctx.redirect(hasValidUser(ctx.req().getSession()) ? "/dashboard" : "/login");
    }

    public void showLogin(Context ctx) throws DatabaseException {
        String created = ctx.queryParam("created");
        String logout = ctx.queryParam("logout");
        if (hasValidUser(ctx.req().getSession())) {
            ctx.redirect("/dashboard");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        LoginForm form = new LoginForm();
        if (created != null) {
            form.setUsername(created);
            model.put("success", "Brugeren \"" + created + "\" er oprettet. Log ind herunder.");
        } else if (logout != null) {
            model.put("success", "Du er nu logget ud.");
        }
        model.put("form", form);
        ctx.render("login", model);
    }

    public void login(Context ctx) throws DatabaseException {
        LoginForm form = new LoginForm();
        form.setUsername(ctx.formParam("username"));
        form.setPassword(ctx.formParam("password"));

        Map<String, Object> model = new HashMap<>();
        model.put("form", form);

        if (form.getUsername() == null || form.getUsername().isBlank()) {
            loginError(ctx, model, "username", "Du skal indtaste et brugernavn.");
            return;
        }
        if (form.getPassword() == null || form.getPassword().isEmpty()) {
            loginError(ctx, model, "password", "Du skal indtaste et kodeord.");
            return;
        }

        Optional<User> user = userService.login(form.getUsername(), form.getPassword());
        if (user.isEmpty()) {
            loginError(ctx, model, "both", "Forkert brugernavn eller kodeord.");
            return;
        }

        SessionUtil.login(ctx.req().getSession(), user.get().getId());

        // Første gang skal brugeren igennem spørgeskemaet
        ctx.redirect(user.get().hasCompletedOnboarding() ? "/dashboard" : "/onboarding");
    }

    public void showRegister(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("form", new RegisterForm());
        ctx.render("register", model);
    }

    public void register(Context ctx) throws DatabaseException {
        RegisterForm form = new RegisterForm();
        form.setUsername(ctx.formParam("username"));
        form.setEmail(ctx.formParam("email"));
        form.setPassword(ctx.formParam("password"));
        form.setPassword2(ctx.formParam("password2"));

        FormError error = userService.validateRegistration(form);
        if (error != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("form", form);
            model.put("error", error.getMessage());
            model.put("errorField", error.getField());
            ctx.render("register", model);
            return;
        }

        User user = userService.register(form);
        ctx.redirect("/login?created=" + user.getUsername());
    }

    public void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/login?logout=1");
    }

    // Sessionen kan pege på en bruger der ikke findes mere efter en genstart.
    // Så rydder vi den, ellers sender login og dashboard brugeren frem og tilbage
    private boolean hasValidUser(HttpSession session) throws DatabaseException {
        if (userService.findById(SessionUtil.getUserId(session)).isPresent()) {
            return true;
        }
        if (SessionUtil.isLoggedIn(session)) {
            session.invalidate();
        }
        return false;
    }

    private void loginError(Context ctx, Map<String, Object> model, String field, String message) {
        model.put("error", message);
        model.put("errorField", field);
        ctx.render("login", model);
    }
}

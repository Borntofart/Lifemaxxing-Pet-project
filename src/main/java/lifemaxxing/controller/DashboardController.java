package lifemaxxing.controller;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import lifemaxxing.model.User;
import lifemaxxing.service.DashboardService;
import lifemaxxing.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    private final UserService userService;
    private final DashboardService dashboardService;

    public DashboardController(UserService userService, DashboardService dashboardService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
    }

    public void setRoutes(JavalinConfig config) {
        config.routes.get("/dashboard", this::dashboard);
        config.routes.post("/dashboard/vaegt", this::addWeighIn);
    }

    public void dashboard(Context ctx) {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }
        if (!user.hasCompletedOnboarding()) {
            ctx.redirect("/onboarding");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("view", dashboardService.buildView(user));
        model.put("weighInError", ctx.consumeSessionAttribute("weighInError"));
        model.put("weighInSuccess", ctx.consumeSessionAttribute("weighInSuccess"));
        ctx.render("dashboard", model);
    }

    public void addWeighIn(Context ctx) {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }
        if (!user.hasCompletedOnboarding()) {
            ctx.redirect("/onboarding");
            return;
        }

        String error = dashboardService.registerWeighIn(user, parseFloat(ctx.formParam("weightKg")));
        if (error != null) {
            ctx.sessionAttribute("weighInError", error);
        } else {
            ctx.sessionAttribute("weighInSuccess", "Vægten er gemt og din kostplan er opdateret.");
        }
        ctx.redirect("/dashboard");
    }

    private Float parseFloat(String value) {
        try {
            return value == null || value.isBlank() ? null : Float.parseFloat(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package lifemaxxing;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import lifemaxxing.controller.AuthController;
import lifemaxxing.controller.DashboardController;
import lifemaxxing.controller.OnboardingController;
import lifemaxxing.repository.ExerciseCatalog;
import lifemaxxing.repository.UserRepository;
import lifemaxxing.service.DashboardService;
import lifemaxxing.service.MacroService;
import lifemaxxing.service.OnboardingService;
import lifemaxxing.service.UserService;
import lifemaxxing.service.WorkoutService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

public class LifemaxxingApplication {

    public static void main(String[] args) {
        UserRepository userRepository = new UserRepository();
        ExerciseCatalog catalog = new ExerciseCatalog();

        MacroService macroService = new MacroService();
        WorkoutService workoutService = new WorkoutService(catalog);
        UserService userService = new UserService(userRepository);
        OnboardingService onboardingService = new OnboardingService(userRepository, macroService);
        DashboardService dashboardService = new DashboardService(workoutService, macroService, userRepository);

        AuthController authController = new AuthController(userService);
        DashboardController dashboardController = new DashboardController(userService, dashboardService);
        OnboardingController onboardingController = new OnboardingController(userService, onboardingService);

        Javalin.create(config -> {
            config.jetty.port = 8080;
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinThymeleaf(templateEngine()));

            // Sesion udløber efter 60 min
            config.jetty.modifyServletContextHandler(handler ->
                    handler.getSessionHandler().setMaxInactiveInterval(60 * 60));

            authController.setRoutes(config);
            dashboardController.setRoutes(config);
            onboardingController.setRoutes(config);
        }).start();
    }

    private static TemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        // Slår cache fra så html ændringer kan ses med det samme
        resolver.setCacheable(false);

        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}

package lifemaxxing;

import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import lifemaxxing.controller.AuthController;
import lifemaxxing.controller.DashboardController;
import lifemaxxing.controller.OnboardingController;
import lifemaxxing.controller.TrainingController;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.persistence.ConnectionPool;
import lifemaxxing.persistence.ExerciseMapper;
import lifemaxxing.persistence.ProgramMapper;
import lifemaxxing.persistence.UserMapper;
import lifemaxxing.persistence.WeighInMapper;
import lifemaxxing.persistence.WorkoutLogMapper;
import lifemaxxing.service.DashboardService;
import lifemaxxing.service.MacroService;
import lifemaxxing.service.OnboardingService;
import lifemaxxing.service.TrainingLogService;
import lifemaxxing.service.UserService;
import lifemaxxing.service.WorkoutService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Map;

public class LifemaxxingApplication {

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "lifemaxxing";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) {
        UserMapper userMapper = new UserMapper(connectionPool);
        WeighInMapper weighInMapper = new WeighInMapper(connectionPool);
        ProgramMapper programMapper = new ProgramMapper(connectionPool);
        ExerciseMapper exerciseMapper = new ExerciseMapper(connectionPool);
        WorkoutLogMapper workoutLogMapper = new WorkoutLogMapper(connectionPool);

        MacroService macroService = new MacroService();
        WorkoutService workoutService = new WorkoutService(exerciseMapper, programMapper);
        UserService userService = new UserService(userMapper, weighInMapper, programMapper);
        OnboardingService onboardingService = new OnboardingService(userMapper, weighInMapper, programMapper,
                macroService, workoutService);
        DashboardService dashboardService = new DashboardService(workoutService, macroService, weighInMapper, programMapper);
        TrainingLogService trainingLogService = new TrainingLogService(workoutLogMapper);

        AuthController authController = new AuthController(userService);
        DashboardController dashboardController = new DashboardController(userService, dashboardService);
        OnboardingController onboardingController = new OnboardingController(userService, onboardingService);
        TrainingController trainingController = new TrainingController(userService, workoutService, trainingLogService);

        Javalin.create(config -> {
            // pgAdmin kører på 8080 i docker, så vi bruger 7070
            config.jetty.port = 7070;
            config.staticFiles.add("/static");
            config.fileRenderer(new JavalinThymeleaf(templateEngine()));

            // Sesion udløber efter 60 min
            config.jetty.modifyServletContextHandler(handler ->
                    handler.getSessionHandler().setMaxInactiveInterval(60 * 60));

            authController.setRoutes(config);
            dashboardController.setRoutes(config);
            onboardingController.setRoutes(config);
            trainingController.setRoutes(config);

            // Fejl fra databasen havner her i stedet for at hver controller selv skal fange dem
            config.routes.exception(DatabaseException.class, (e, ctx) -> {
                ctx.status(500);
                ctx.render("fejl", Map.of("message", e.getMessage()));
            });
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

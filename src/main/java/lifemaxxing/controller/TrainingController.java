package lifemaxxing.controller;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.LoggedSet;
import lifemaxxing.model.PlannedExercise;
import lifemaxxing.model.User;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.service.TrainingLogService;
import lifemaxxing.service.UserService;
import lifemaxxing.service.WorkoutService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Siden hvor brugeren logger sine sæt og ser sine rekorder
public class TrainingController {

    private static final String[] DAY_NAMES = {"Mandag", "Tirsdag", "Onsdag", "Torsdag", "Fredag", "Lørdag", "Søndag"};

    private final UserService userService;
    private final WorkoutService workoutService;
    private final TrainingLogService trainingLogService;

    public TrainingController(UserService userService, WorkoutService workoutService, TrainingLogService trainingLogService) {
        this.userService = userService;
        this.workoutService = workoutService;
        this.trainingLogService = trainingLogService;
    }

    public void setRoutes(JavalinConfig config) {
        config.routes.get("/traening", this::showLog);
        config.routes.post("/traening", this::saveLog);
    }

    public void showLog(Context ctx) throws DatabaseException {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }
        if (!user.hasCompletedOnboarding()) {
            ctx.redirect("/onboarding");
            return;
        }

        // Uden ?dag= vises dagens træning
        int dayNr = parseDay(ctx.queryParam("dag"), LocalDate.now().getDayOfWeek().getValue());
        List<WorkoutDay> week = workoutService.getWeek(user);

        Map<String, Object> model = new HashMap<>();
        model.put("username", user.getUsername());
        model.put("initials", user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase());
        model.put("week", week);
        model.put("dayNames", DAY_NAMES);
        model.put("day", findDay(week, dayNr));
        model.put("records", trainingLogService.getRecords(user));
        model.put("sessions", trainingLogService.getRecentSessions(user));
        model.put("logSuccess", ctx.consumeSessionAttribute("logSuccess"));
        model.put("logError", ctx.consumeSessionAttribute("logError"));
        model.put("newRecords", ctx.consumeSessionAttribute("newRecords"));
        ctx.render("traening", model);
    }

    public void saveLog(Context ctx) throws DatabaseException {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }
        if (!user.hasCompletedOnboarding()) {
            ctx.redirect("/onboarding");
            return;
        }

        int dayNr = parseDay(ctx.formParam("dag"), 0);
        WorkoutDay day = findDay(workoutService.getWeek(user), dayNr);
        if (day.isRestDay()) {
            ctx.sessionAttribute("logError", "Der er ingen træning at logge den dag.");
            ctx.redirect("/traening");
            return;
        }

        List<LoggedSet> sets = new ArrayList<>();
        String error = readSets(ctx, day, sets);
        boolean onlyTimed = day.getExercises().stream().allMatch(PlannedExercise::isTimed);
        if (error == null && sets.isEmpty() && !onlyTimed) {
            error = "Udfyld reps på mindst ét sæt.";
        }
        if (error != null) {
            ctx.sessionAttribute("logError", error);
            ctx.redirect("/traening?dag=" + dayNr);
            return;
        }

        String notes = ctx.formParam("noter");
        notes = notes == null || notes.isBlank() ? null : notes.trim();
        List<String> newRecords = trainingLogService.logSession(user, day, sets, notes);

        ctx.sessionAttribute("logSuccess", "Træningen er gemt.");
        ctx.sessionAttribute("newRecords", newRecords);
        ctx.redirect("/traening?dag=" + dayNr);
    }

    // Felterne hedder reps_<planlagt øvelse>_<sæt nr> og kg_<planlagt øvelse>_<sæt nr>.
    // Sæt uden reps springes over, så man ikke behøver lave dem alle sammen
    private String readSets(Context ctx, WorkoutDay day, List<LoggedSet> sets) {
        for (PlannedExercise ex : day.getExercises()) {
            if (ex.isTimed()) {
                continue;
            }
            for (int setNr = 1; setNr <= ex.getSets(); setNr++) {
                String repsText = ctx.formParam("reps_" + ex.getId() + "_" + setNr);
                String kgText = ctx.formParam("kg_" + ex.getId() + "_" + setNr);
                if (repsText == null || repsText.isBlank()) {
                    continue;
                }

                int reps;
                float kg;
                try {
                    reps = Integer.parseInt(repsText.trim());
                    kg = kgText == null || kgText.isBlank() ? 0 : Float.parseFloat(kgText.trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    return "Brug kun tal i reps og kg ved " + ex.getName() + ".";
                }
                if (reps < 1 || reps > 100) {
                    return "Reps skal være mellem 1 og 100 ved " + ex.getName() + ".";
                }
                if (kg < 0 || kg > 500) {
                    return "Vægten skal være mellem 0 og 500 kg ved " + ex.getName() + ".";
                }
                sets.add(new LoggedSet(ex, setNr, reps, kg));
            }
        }
        return null;
    }

    private WorkoutDay findDay(List<WorkoutDay> week, int dayNr) {
        for (WorkoutDay day : week) {
            if (day.getDayNr() == dayNr) {
                return day;
            }
        }
        return WorkoutDay.restDay(dayNr);
    }

    private int parseDay(String value, int fallback) {
        try {
            int day = Integer.parseInt(value);
            return day >= 1 && day <= 7 ? day : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

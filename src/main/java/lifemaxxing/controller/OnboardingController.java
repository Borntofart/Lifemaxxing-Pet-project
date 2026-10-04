package lifemaxxing.controller;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;
import lifemaxxing.dto.FormError;
import lifemaxxing.dto.OnboardingForm;
import lifemaxxing.model.FitnessGoal.GoalType;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile.ExperienceLevel;
import lifemaxxing.model.UserProfile.Gender;
import lifemaxxing.service.OnboardingService;
import lifemaxxing.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Spørgeskemaet. Bruges både første gang og når brugeren vil rette sine svar
public class OnboardingController {

    private final UserService userService;
    private final OnboardingService onboardingService;

    public OnboardingController(UserService userService, OnboardingService onboardingService) {
        this.userService = userService;
        this.onboardingService = onboardingService;
    }

    public void setRoutes(JavalinConfig config) {
        config.routes.get("/onboarding", this::showForm);
        config.routes.post("/onboarding", this::submit);
    }

    // Valgmulighederne til radio-knapperne i html
    private Map<String, Object> newModel() {
        Map<String, Object> model = new HashMap<>();
        model.put("genders", Gender.values());
        model.put("levels", ExperienceLevel.values());
        model.put("goalTypes", GoalType.values());
        model.put("physicalOptions", List.of("Stærkere", "Mere toned", "Eksplosiv", "Større"));
        model.put("durations", List.of(30, 45, 60, 90));
        return model;
    }

    public void showForm(Context ctx) {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        Map<String, Object> model = newModel();
        model.put("form", onboardingService.toForm(user));
        model.put("editing", user.hasCompletedOnboarding());
        ctx.render("onboarding", model);
    }

    public void submit(Context ctx) {
        User user = userService.findById(SessionUtil.getUserId(ctx.req().getSession())).orElse(null);
        if (user == null) {
            ctx.redirect("/login");
            return;
        }
        Map<String, Object> model = newModel();
        model.put("editing", user.hasCompletedOnboarding());

        OnboardingForm form = new OnboardingForm();
        model.put("form", form);
        String badField = bind(ctx, form);

        // Bogstaver i et talfelt o.l. fanges her før vi når til servicen
        if (badField != null) {
            model.put("error", "Et eller flere felter er ikke udfyldt korrekt. Brug kun tal i tal-felterne.");
            model.put("errorField", badField);
            ctx.render("onboarding", model);
            return;
        }

        FormError error = onboardingService.validate(form);
        if (error != null) {
            model.put("error", error.getMessage());
            model.put("errorField", error.getField());
            ctx.render("onboarding", model);
            return;
        }

        onboardingService.complete(user, form);
        ctx.redirect("/dashboard");
    }

    // Fylder formen ud fra request. Returnerer navnet på første felt der ikke kunne læses
    private String bind(Context ctx, OnboardingForm form) {
        String badField = null;

        try { form.setAge(toInteger(ctx.formParam("age"))); } catch (IllegalArgumentException e) { badField = first(badField, "age"); }
        try { form.setGender(toEnum(Gender.class, ctx.formParam("gender"))); } catch (IllegalArgumentException e) { badField = first(badField, "gender"); }
        try { form.setHeightCm(toFloat(ctx.formParam("heightCm"))); } catch (IllegalArgumentException e) { badField = first(badField, "heightCm"); }
        try { form.setWeightKg(toFloat(ctx.formParam("weightKg"))); } catch (IllegalArgumentException e) { badField = first(badField, "weightKg"); }
        try { form.setExperienceLevel(toEnum(ExperienceLevel.class, ctx.formParam("experienceLevel"))); } catch (IllegalArgumentException e) { badField = first(badField, "experienceLevel"); }
        form.setInjuryNotes(ctx.formParam("injuryNotes"));

        try { form.setGoalType(toEnum(GoalType.class, ctx.formParam("goalType"))); } catch (IllegalArgumentException e) { badField = first(badField, "goalType"); }
        try { form.setTargetWeightKg(toFloat(ctx.formParam("targetWeightKg"))); } catch (IllegalArgumentException e) { badField = first(badField, "targetWeightKg"); }
        try { form.setDeadline(toDate(ctx.formParam("deadline"))); } catch (RuntimeException e) { badField = first(badField, "deadline"); }
        try { form.setWeeklyWeightChangeKg(toFloat(ctx.formParam("weeklyWeightChangeKg"))); } catch (IllegalArgumentException e) { badField = first(badField, "weeklyWeightChangeKg"); }
        form.setPhysicalGoals(new ArrayList<>(ctx.formParams("physicalGoals")));

        if (ctx.formParam("focus") != null) {
            form.setFocus(ctx.formParam("focus"));
        }
        form.setWantsStrength(ctx.formParam("wantsStrength") != null);
        form.setWantsCardio(ctx.formParam("wantsCardio") != null);
        if (ctx.formParam("trainingStyle") != null) {
            form.setTrainingStyle(ctx.formParam("trainingStyle"));
        }
        try {
            if (ctx.formParam("trainingDaysPerWeek") != null) {
                form.setTrainingDaysPerWeek(toInteger(ctx.formParam("trainingDaysPerWeek")));
            }
        } catch (IllegalArgumentException e) { badField = first(badField, "trainingDaysPerWeek"); }
        try {
            if (ctx.formParam("sessionDurationMin") != null) {
                form.setSessionDurationMin(toInteger(ctx.formParam("sessionDurationMin")));
            }
        } catch (IllegalArgumentException e) { badField = first(badField, "sessionDurationMin"); }

        return badField;
    }

    private String first(String current, String field) {
        return current != null ? current : field;
    }

    private Integer toInteger(String value) {
        return isEmpty(value) ? null : Integer.valueOf(value.trim());
    }

    private Float toFloat(String value) {
        return isEmpty(value) ? null : Float.valueOf(value.trim());
    }

    private LocalDate toDate(String value) {
        return isEmpty(value) ? null : LocalDate.parse(value.trim());
    }

    private <E extends Enum<E>> E toEnum(Class<E> type, String value) {
        return isEmpty(value) ? null : Enum.valueOf(type, value.trim());
    }

    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}

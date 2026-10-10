package lifemaxxing.service;

import lifemaxxing.dto.FormError;
import lifemaxxing.dto.OnboardingForm;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.FitnessGoal.GoalType;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.WeighIn;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.persistence.ProgramMapper;
import lifemaxxing.persistence.UserMapper;
import lifemaxxing.persistence.WeighInMapper;

import java.time.LocalDate;
import java.util.List;

// Tager svarene fra spørgeskemaet og laver profil, mål, præferencer, makroplan og træningsprogram
public class OnboardingService {

    private static final List<String> FOCUS_OPTIONS = List.of("Styrke", "Cardio", "HIIT", "Blanding af alt");
    private static final List<String> STYLE_OPTIONS = List.of("Bodybuilder", "Atlet");
    private static final List<String> PHYSICAL_OPTIONS = List.of("Stærkere", "Mere toned", "Eksplosiv", "Større");
    private static final List<Integer> DURATION_OPTIONS = List.of(30, 45, 60, 90);

    private final UserMapper userMapper;
    private final WeighInMapper weighInMapper;
    private final ProgramMapper programMapper;
    private final MacroService macroService;
    private final WorkoutService workoutService;

    public OnboardingService(UserMapper userMapper, WeighInMapper weighInMapper, ProgramMapper programMapper,
                             MacroService macroService, WorkoutService workoutService) {
        this.userMapper = userMapper;
        this.weighInMapper = weighInMapper;
        this.programMapper = programMapper;
        this.macroService = macroService;
        this.workoutService = workoutService;
    }

    public FormError validate(OnboardingForm f) {
        if (f.getAge() == null || f.getAge() < 10 || f.getAge() > 100) {
            return new FormError("age", "Alder skal være mellem 10 og 100.");
        }
        if (f.getGender() == null) {
            return new FormError("gender", "Vælg dit køn.");
        }
        if (f.getHeightCm() == null || f.getHeightCm() < 100 || f.getHeightCm() > 250) {
            return new FormError("heightCm", "Højde skal være mellem 100 og 250 cm.");
        }
        if (f.getWeightKg() == null || f.getWeightKg() < 30 || f.getWeightKg() > 300) {
            return new FormError("weightKg", "Vægt skal være mellem 30 og 300 kg.");
        }
        if (f.getExperienceLevel() == null) {
            return new FormError("experienceLevel", "Vælg dit erfaringsniveau.");
        }
        if (f.getGoalType() == null) {
            return new FormError("goalType", "Vælg dit mål.");
        }
        if (f.getTargetWeightKg() == null || f.getTargetWeightKg() < 30 || f.getTargetWeightKg() > 300) {
            return new FormError("targetWeightKg", "Målvægt skal være mellem 30 og 300 kg.");
        }
        if (f.getGoalType() == GoalType.LOSE_WEIGHT && f.getTargetWeightKg() >= f.getWeightKg()) {
            return new FormError("targetWeightKg", "Vil du tabe dig, skal målvægten være lavere end din vægt.");
        }
        if (f.getGoalType() == GoalType.BULK_UP && f.getTargetWeightKg() <= f.getWeightKg()) {
            return new FormError("targetWeightKg", "Vil du tage på, skal målvægten være højere end din vægt.");
        }
        if (f.getWeeklyWeightChangeKg() != null && Math.abs(f.getWeeklyWeightChangeKg()) > 1.5f) {
            return new FormError("weeklyWeightChangeKg", "Mere end 1,5 kg om ugen er ikke sundt. Vælg et lavere tal.");
        }
        if (f.getDeadline() != null && !f.getDeadline().isAfter(LocalDate.now())) {
            return new FormError("deadline", "Deadline skal ligge ude i fremtiden.");
        }
        if (f.getPhysicalGoals() != null && f.getPhysicalGoals().size() > 2) {
            return new FormError("physicalGoals", "Vælg højst 2 fysiske delmål.");
        }
        if (f.getPhysicalGoals() != null && !PHYSICAL_OPTIONS.containsAll(f.getPhysicalGoals())) {
            return new FormError("physicalGoals", "Ugyldigt delmål.");
        }
        if (!FOCUS_OPTIONS.contains(f.getFocus())) {
            return new FormError("focus", "Vælg et træningsfokus.");
        }
        if (!STYLE_OPTIONS.contains(f.getTrainingStyle())) {
            return new FormError("trainingStyle", "Vælg en træningsstil.");
        }
        if (f.getTrainingDaysPerWeek() == null || f.getTrainingDaysPerWeek() < 1 || f.getTrainingDaysPerWeek() > 7) {
            return new FormError("trainingDaysPerWeek", "Vælg mellem 1 og 7 træningsdage.");
        }
        if (!DURATION_OPTIONS.contains(f.getSessionDurationMin())) {
            return new FormError("sessionDurationMin", "Vælg en længde på træningen.");
        }
        return null;
    }

    // Kald kun efter validate har givet null
    public void complete(User user, OnboardingForm f) throws DatabaseException {
        float weight = f.getWeightKg();

        UserProfile profile = new UserProfile(f.getAge(), f.getGender(), f.getHeightCm(), weight,
                f.getExperienceLevel(), f.getInjuryNotes() == null ? "" : f.getInjuryNotes().trim());

        // Hvsi brugeren retter i sine svar og målet er det samme, beholdes startvægten
        float startWeight = weight;
        if (user.getGoal() != null && user.getGoal().getGoalType() == f.getGoalType()) {
            startWeight = user.getGoal().getStartWeightKg();
        }

        float weekly = resolveWeeklyChange(f.getGoalType(), f.getWeeklyWeightChangeKg());
        LocalDate deadline = f.getDeadline() != null
                ? f.getDeadline()
                : estimateDeadline(weight, f.getTargetWeightKg(), weekly);

        FitnessGoal goal = new FitnessGoal(f.getGoalType(), f.getTargetWeightKg(), startWeight,
                deadline, weekly, f.getPhysicalGoals());

        TrainingPreference prefs = buildPreference(f);

        user.setProfile(profile);
        user.setGoal(goal);
        user.setPreference(prefs);
        user.setMacroPlan(macroService.calculateFor(profile, goal, prefs));

        WeighIn last = user.getLatestWeighIn();
        if (last == null || last.getWeightKg() != weight) {
            WeighIn weighIn = new WeighIn(LocalDate.now(), weight);
            user.addWeighIn(weighIn);
            weighInMapper.saveWeighIn(user.getId(), weighIn);
        }

        userMapper.saveProfile(user, profile);

        // Et nyt program hver gang svarene gemmes. Det gamle bliver afsluttet men ikke slettet
        List<WorkoutDay> week = workoutService.generateWeek(user);
        int programId = programMapper.createProgram(user.getId(), prefs.getRecommendedSplit(), goal, prefs,
                profile.getExperienceLevel(), user.getMacroPlan(), week);
        user.setActiveProgramId(programId);
    }

    // Fylder formularen ud med de svar brugeren allerede har givet
    public OnboardingForm toForm(User user) {
        OnboardingForm f = new OnboardingForm();
        if (!user.hasCompletedOnboarding()) {
            return f;
        }
        UserProfile p = user.getProfile();
        FitnessGoal g = user.getGoal();
        TrainingPreference t = user.getPreference();

        f.setAge(p.getAge());
        f.setGender(p.getGender());
        f.setHeightCm(p.getHeightCm());
        f.setWeightKg(p.getWeightKg());
        f.setExperienceLevel(p.getExperienceLevel());
        f.setInjuryNotes(p.getInjuryNotes());
        f.setGoalType(g.getGoalType());
        f.setTargetWeightKg(g.getTargetWeightKg());
        f.setDeadline(g.getDeadline());
        f.setWeeklyWeightChangeKg(Math.abs(g.getWeeklyWeightChangeKg()));
        f.setPhysicalGoals(g.getPhysicalGoals());
        f.setFocus(t.getFocus());
        f.setWantsStrength(t.isWantsStrength());
        f.setWantsCardio(t.isWantsCardio());
        f.setTrainingStyle(t.getTrainingStyle());
        f.setTrainingDaysPerWeek(t.getTrainingDaysPerWeek());
        f.setSessionDurationMin(t.getSessionDurationMin());
        return f;
    }

    private TrainingPreference buildPreference(OnboardingForm f) {
        boolean strength = f.isWantsStrength();
        boolean cardio = f.isWantsCardio();

        // Har brugeren ikke krydset noget af, bruges standard ud fra fokus
        if (!strength && !cardio) {
            strength = "Styrke".equals(f.getFocus()) || "Blanding af alt".equals(f.getFocus());
            cardio = !"Styrke".equals(f.getFocus());
        }

        // Ren cardio eller HIIT uden styrke giver ikke mening som bodybuilder
        String style = f.getTrainingStyle();
        boolean cardioOnly = ("Cardio".equals(f.getFocus()) || "HIIT".equals(f.getFocus())) && !strength;
        if (cardioOnly) {
            style = "Atlet";
        }

        return new TrainingPreference(f.getFocus(), style, strength, cardio,
                f.getTrainingDaysPerWeek(), f.getSessionDurationMin());
    }

    // Fortegnet styres af målet, så brugeren bare skriver fx 0,5
    private float resolveWeeklyChange(GoalType type, Float input) {
        float value = input == null ? defaultWeekly(type) : Math.abs(input);
        return switch (type) {
            case LOSE_WEIGHT -> -value;
            case BULK_UP -> value;
            default -> 0f;
        };
    }

    private float defaultWeekly(GoalType type) {
        return switch (type) {
            case LOSE_WEIGHT -> 0.5f;
            case BULK_UP -> 0.3f;
            default -> 0f;
        };
    }

    private LocalDate estimateDeadline(float current, float target, float weekly) {
        float diff = Math.abs(target - current);
        if (diff < 0.1f || Math.abs(weekly) < 0.01f) {
            return LocalDate.now().plusWeeks(12);
        }
        long weeks = (long) Math.ceil(diff / Math.abs(weekly));
        return LocalDate.now().plusWeeks(weeks);
    }
}

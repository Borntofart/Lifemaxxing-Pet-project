package lifemaxxing.service;

import lifemaxxing.dto.DashboardView;
import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.WeighIn;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class DashboardService {

    private static final Locale DK = new Locale("da", "DK");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d. MMM yyyy", DK);

    private final WorkoutService workoutService;
    private final MacroService macroService;
    private final UserRepository userRepository;

    public DashboardService(WorkoutService workoutService, MacroService macroService,
                            UserRepository userRepository) {
        this.workoutService = workoutService;
        this.macroService = macroService;
        this.userRepository = userRepository;
    }

    public DashboardView buildView(User user) {
        UserProfile profile = user.getProfile();
        FitnessGoal goal = user.getGoal();
        TrainingPreference prefs = user.getPreference();
        float current = profile.getWeightKg();
        LocalDate today = LocalDate.now();

        DashboardView view = new DashboardView();
        view.setUsername(user.getUsername());
        view.setInitials(user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase());

        // Mål kortet
        view.setGoalTitle(goalTitle(goal, current));
        view.setProgressPercent(goal.calculateProgressPercent(current));
        view.setStartWeight(kg(goal.getStartWeightKg()));
        view.setCurrentWeight(kg(current));
        view.setTargetWeight(kg(goal.getTargetWeightKg()));
        view.setDeadline(goal.getDeadline().format(DATE));

        // Dagens træning
        WorkoutDay todays = workoutService.getWorkoutFor(user, today);
        view.setTodaysWorkout(todays);
        if (todays.isRestDay()) {
            WorkoutDay next = workoutService.getNextWorkout(user, today);
            view.setNextWorkoutTitle(next != null ? next.getTitle() : null);
        }
        view.setSplitText(prefs.getRecommendedSplit());

        // Kost
        view.setMacroPlan(user.getMacroPlan());
        view.setMeals(macroService.splitIntoMeals(user.getMacroPlan()));

        // Fremgang
        view.setWeightChange(String.format(DK, "%+.1f kg", current - goal.getStartWeightKg()));
        view.setTrainingDays(prefs.getTrainingDaysPerWeek());
        view.setSessionLength(prefs.getSessionDurationMin());
        WeighIn last = user.getLatestWeighIn();
        view.setLastWeighIn(last != null ? last.getDate().format(DATE) : null);

        // Skade
        view.setInjury(profile.hasInjury());
        view.setInjuryNotes(profile.getInjuryNotes());

        return view;
    }

    // Ny vejning opdaterer også makroplanen, da den regnes ud fra vægten
    public String registerWeighIn(User user, Float weightKg) {
        if (weightKg == null || weightKg < 30 || weightKg > 300) {
            return "Vægten skal være mellem 30 og 300 kg.";
        }
        user.addWeighIn(new WeighIn(LocalDate.now(), weightKg));
        user.getProfile().setWeightKg(weightKg);
        user.setMacroPlan(macroService.calculateFor(user.getProfile(), user.getGoal(), user.getPreference()));
        userRepository.save(user);
        return null;
    }

    private String goalTitle(FitnessGoal goal, float current) {
        String base = switch (goal.getGoalType()) {
            case LOSE_WEIGHT -> "Tabe " + number(goal.getStartWeightKg() - goal.getTargetWeightKg()) + " kg";
            case BULK_UP -> "Tage " + number(goal.getTargetWeightKg() - goal.getStartWeightKg()) + " kg på";
            case MAINTAIN -> "Holde vægten på " + number(goal.getTargetWeightKg()) + " kg";
            case RECOMP -> "Tabe fedt og bygge muskler";
        };

        List<String> physical = goal.getPhysicalGoals();
        if (physical.isEmpty()) {
            return base;
        }
        return base + " og " + physicalPhrase(physical.get(0));
    }

    private String physicalPhrase(String goal) {
        return switch (goal) {
            case "Stærkere" -> "blive stærkere";
            case "Mere toned" -> "komme i bedre form";
            case "Eksplosiv" -> "blive mere eksplosiv";
            case "Større" -> "bygge muskler";
            default -> goal.toLowerCase();
        };
    }

    private String kg(float value) {
        return number(value) + " kg";
    }

    // Viser 82 i stedet for 82,0 men beholder 78,6
    private String number(float value) {
        float rounded = Math.round(value * 10) / 10f;
        if (rounded == Math.round(rounded)) {
            return String.valueOf(Math.round(rounded));
        }
        return String.format(DK, "%.1f", rounded);
    }
}

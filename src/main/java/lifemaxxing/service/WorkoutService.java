package lifemaxxing.service;

import lifemaxxing.model.Exercise;
import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.PlannedExercise;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.UserProfile.ExperienceLevel;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.repository.ExerciseCatalog;
import lifemaxxing.repository.ExerciseCatalog.Template;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static lifemaxxing.repository.ExerciseCatalog.*;

// Bygger brugerens uge ud fra svarene i spørgeskemaet
public class WorkoutService {

    // Hvilke ugedage der trænes (0 = mandag) alt efter antal dage
    private static final int[][] WEEK_PATTERNS = {
            {}, {0}, {0, 3}, {0, 2, 4}, {0, 1, 3, 4}, {0, 1, 2, 3, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 4, 5, 6}
    };

    private final ExerciseCatalog catalog;

    public WorkoutService(ExerciseCatalog catalog) {
        this.catalog = catalog;
    }

    public List<WorkoutDay> buildWeek(User user) {
        List<WorkoutDay> week = new ArrayList<>();
        for (String key : templateKeys(user.getPreference())) {
            week.add(buildDay(catalog.getTemplate(key), user));
        }
        return week;
    }

    public WorkoutDay getWorkoutFor(User user, LocalDate date) {
        int[] pattern = WEEK_PATTERNS[user.getPreference().getTrainingDaysPerWeek()];
        int dayIndex = date.getDayOfWeek().getValue() - 1;

        List<WorkoutDay> week = buildWeek(user);
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] == dayIndex) {
                return week.get(i);
            }
        }
        return WorkoutDay.restDay();
    }

    // Finder næste dag der ikke er hviledag
    public WorkoutDay getNextWorkout(User user, LocalDate from) {
        for (int i = 1; i <= 7; i++) {
            WorkoutDay day = getWorkoutFor(user, from.plusDays(i));
            if (!day.isRestDay()) {
                return day;
            }
        }
        return null;
    }

    private List<String> templateKeys(TrainingPreference pref) {
        List<String> keys = new ArrayList<>();
        int days = pref.getTrainingDaysPerWeek();
        String cardioKey = "HIIT".equals(pref.getFocus()) ? HIIT : CARDIO;

        for (int i = 0; i < days; i++) {
            switch (pref.getSplitType()) {
                case FULL_BODY -> keys.add(i % 2 == 0 ? FULL_A : FULL_B);
                case UPPER_LOWER -> keys.add(i % 2 == 0 ? UPPER : LOWER);
                case PPL -> keys.add(new String[]{PUSH, PULL, LEGS}[i % 3]);
                case CARDIO -> keys.add(CARDIO);
                case HIIT -> keys.add(HIIT);
                case MIX -> keys.add(i % 2 == 1 ? cardioKey : (i % 4 == 0 ? FULL_A : FULL_B));
            }
        }
        return keys;
    }

    private WorkoutDay buildDay(Template template, User user) {
        int max = maxExercises(user.getPreference().getSessionDurationMin());
        List<PlannedExercise> planned = new ArrayList<>();

        for (Exercise ex : template.getExercises()) {
            if (planned.size() >= max) break;
            planned.add(plan(ex, user));
        }
        return new WorkoutDay(template.getTitle(), template.getCategory(), planned);
    }

    private PlannedExercise plan(Exercise ex, User user) {
        UserProfile profile = user.getProfile();
        boolean athlete = "Atlet".equals(user.getPreference().getTrainingStyle());

        return switch (ex.getType()) {
            case CARDIO, HIIT -> new PlannedExercise(ex.getName(), ex.getDosage());
            case COMPOUND -> new PlannedExercise(ex.getName(),
                    sets(4, profile, true) + " x " + compoundReps(user.getGoal(), athlete));
            case ISOLATION -> new PlannedExercise(ex.getName(),
                    sets(3, profile, false) + " x " + (athlete ? "8-10" : "10-15"));
        };
    }

    // Fysiske delmål styrer rep-området på de tunge øvelser
    private String compoundReps(FitnessGoal goal, boolean athlete) {
        if (goal.getPhysicalGoals().contains("Stærkere")) return "4-6";
        if (goal.getPhysicalGoals().contains("Eksplosiv")) return "3-5";
        if (goal.getPhysicalGoals().contains("Større")) return "8-10";
        return athlete ? "5-8" : "6-10";
    }

    private int sets(int base, UserProfile profile, boolean compound) {
        ExperienceLevel level = profile.getExperienceLevel();
        if (level == ExperienceLevel.BEGINNER || level == ExperienceLevel.NOVICE) {
            return base - 1;
        }
        if (level == ExperienceLevel.ADVANCED && compound) {
            return base + 1;
        }
        return base;
    }

    private int maxExercises(int sessionMin) {
        if (sessionMin <= 30) return 3;
        if (sessionMin <= 45) return 4;
        if (sessionMin <= 60) return 5;
        return 6;
    }
}

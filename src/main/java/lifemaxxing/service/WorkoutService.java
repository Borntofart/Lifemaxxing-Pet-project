package lifemaxxing.service;

import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.Exercise;
import lifemaxxing.model.Exercise.Type;
import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.PlannedExercise;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.UserProfile.ExperienceLevel;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.persistence.ExerciseMapper;
import lifemaxxing.persistence.ProgramMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Bygger brugerens uge ud fra svarene i spørgeskemaet
public class WorkoutService {

    // Hvilke ugedage der trænes (0 = mandag) alt efter antal dage
    private static final int[][] WEEK_PATTERNS = {
            {}, {0}, {0, 3}, {0, 2, 4}, {0, 1, 3, 4}, {0, 1, 2, 3, 4}, {0, 1, 2, 3, 4, 5}, {0, 1, 2, 3, 4, 5, 6}
    };

    private static final String PUSH = "PUSH";
    private static final String PULL = "PULL";
    private static final String LEGS = "LEGS";
    private static final String UPPER = "UPPER";
    private static final String LOWER = "LOWER";
    private static final String FULL_A = "FULL_A";
    private static final String FULL_B = "FULL_B";
    private static final String CARDIO = "CARDIO";
    private static final String HIIT = "HIIT";

    // En plads i en træningsdag. Øvelsen findes i databasen ud fra muskelgruppe og type
    private record Slot(String muscleGroup, Type type) {}

    // variant bruges til at vælge den næste øvelse i rækken, så fx Full Body B ikke bliver magen til A
    private record Template(String title, int variant, List<Slot> slots) {}

    private static final Map<String, Template> TEMPLATES = Map.of(
            PUSH, new Template("Push – Overkrop", 0, List.of(
                    new Slot("Bryst", Type.COMPOUND), new Slot("Skuldre", Type.COMPOUND),
                    new Slot("Bryst", Type.COMPOUND), new Slot("Triceps", Type.COMPOUND),
                    new Slot("Skuldre", Type.ISOLATION), new Slot("Triceps", Type.ISOLATION))),
            PULL, new Template("Pull – Ryg og biceps", 0, List.of(
                    new Slot("Ryg", Type.COMPOUND), new Slot("Ryg", Type.COMPOUND),
                    new Slot("Ryg", Type.COMPOUND), new Slot("Bagskulder", Type.ISOLATION),
                    new Slot("Biceps", Type.ISOLATION), new Slot("Biceps", Type.ISOLATION))),
            LEGS, new Template("Ben", 0, List.of(
                    new Slot("Quadriceps", Type.COMPOUND), new Slot("Baglår", Type.COMPOUND),
                    new Slot("Quadriceps", Type.COMPOUND), new Slot("Balder", Type.COMPOUND),
                    new Slot("Baglår", Type.ISOLATION), new Slot("Læg", Type.ISOLATION))),
            UPPER, new Template("Upper – Overkrop", 0, List.of(
                    new Slot("Bryst", Type.COMPOUND), new Slot("Ryg", Type.COMPOUND),
                    new Slot("Skuldre", Type.COMPOUND), new Slot("Ryg", Type.COMPOUND),
                    new Slot("Biceps", Type.ISOLATION), new Slot("Triceps", Type.ISOLATION))),
            LOWER, new Template("Lower – Underkrop", 0, List.of(
                    new Slot("Quadriceps", Type.COMPOUND), new Slot("Baglår", Type.COMPOUND),
                    new Slot("Balder", Type.COMPOUND), new Slot("Quadriceps", Type.ISOLATION),
                    new Slot("Baglår", Type.ISOLATION), new Slot("Læg", Type.ISOLATION))),
            FULL_A, new Template("Full Body A", 0, List.of(
                    new Slot("Quadriceps", Type.COMPOUND), new Slot("Bryst", Type.COMPOUND),
                    new Slot("Ryg", Type.COMPOUND), new Slot("Skuldre", Type.COMPOUND),
                    new Slot("Biceps", Type.ISOLATION), new Slot("Core", Type.ISOLATION))),
            FULL_B, new Template("Full Body B", 1, List.of(
                    new Slot("Baglår", Type.COMPOUND), new Slot("Bryst", Type.COMPOUND),
                    new Slot("Ryg", Type.COMPOUND), new Slot("Quadriceps", Type.COMPOUND),
                    new Slot("Skuldre", Type.ISOLATION), new Slot("Triceps", Type.ISOLATION))),
            CARDIO, new Template("Cardio – Kondition", 0, List.of(
                    new Slot("Opvarmning", Type.CARDIO), new Slot("Kondition", Type.CARDIO),
                    new Slot("Kondition", Type.CARDIO), new Slot("Kondition", Type.CARDIO),
                    new Slot("Nedkøling", Type.CARDIO))),
            HIIT, new Template("HIIT – Circuit", 0, List.of(
                    new Slot("Helkrop", Type.HIIT), new Slot("Balder", Type.HIIT),
                    new Slot("Core", Type.HIIT), new Slot("Quadriceps", Type.HIIT),
                    new Slot("Bryst", Type.HIIT), new Slot("Quadriceps", Type.HIIT)))
    );

    private final ExerciseMapper exerciseMapper;
    private final ProgramMapper programMapper;

    public WorkoutService(ExerciseMapper exerciseMapper, ProgramMapper programMapper) {
        this.exerciseMapper = exerciseMapper;
        this.programMapper = programMapper;
    }

    // Laver en ny uge ud fra brugerens svar. Bliver gemt i databasen af OnboardingService
    public List<WorkoutDay> generateWeek(User user) throws DatabaseException {
        List<Exercise> exercises = exerciseMapper.getAllExercises();
        int[] pattern = WEEK_PATTERNS[user.getPreference().getTrainingDaysPerWeek()];
        List<String> keys = templateKeys(user.getPreference());

        List<WorkoutDay> week = new ArrayList<>();
        for (int i = 0; i < keys.size(); i++) {
            week.add(buildDay(TEMPLATES.get(keys.get(i)), pattern[i] + 1, exercises, user));
        }
        return week;
    }

    // Den gemte uge fra databasen
    public List<WorkoutDay> getWeek(User user) throws DatabaseException {
        return programMapper.getWeek(user.getActiveProgramId());
    }

    public WorkoutDay getWorkoutFor(User user, LocalDate date) throws DatabaseException {
        return findDay(getWeek(user), date.getDayOfWeek().getValue());
    }

    // Finder næste dag der ikke er hviledag
    public WorkoutDay getNextWorkout(User user, LocalDate from) throws DatabaseException {
        List<WorkoutDay> week = getWeek(user);
        for (int i = 1; i <= 7; i++) {
            WorkoutDay day = findDay(week, from.plusDays(i).getDayOfWeek().getValue());
            if (!day.isRestDay()) {
                return day;
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

    private WorkoutDay buildDay(Template template, int dayNr, List<Exercise> exercises, User user) {
        int max = maxExercises(user.getPreference().getSessionDurationMin());
        List<Exercise> chosen = new ArrayList<>();

        for (Slot slot : template.slots()) {
            if (chosen.size() >= max) break;
            Exercise ex = pick(exercises, slot, template.variant(), chosen);
            if (ex != null) {
                chosen.add(ex);
            }
        }

        List<PlannedExercise> planned = new ArrayList<>();
        for (Exercise ex : chosen) {
            planned.add(plan(ex, user));
        }
        return new WorkoutDay(0, dayNr, template.title(), planned);
    }

    // Tager en øvelse der passer til pladsen og ikke allerede er med på dagen
    private Exercise pick(List<Exercise> exercises, Slot slot, int variant, List<Exercise> chosen) {
        List<Exercise> candidates = new ArrayList<>();
        for (Exercise ex : exercises) {
            if (ex.getMuscleGroup().equals(slot.muscleGroup()) && ex.getType() == slot.type() && !chosen.contains(ex)) {
                candidates.add(ex);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        return candidates.get(Math.min(variant, candidates.size() - 1));
    }

    // Længere pause på de tunge øvelser giver mere styrke og muskelvækst end korte pauser
    private PlannedExercise plan(Exercise ex, User user) {
        UserProfile profile = user.getProfile();
        boolean athlete = "Atlet".equals(user.getPreference().getTrainingStyle());

        return switch (ex.getType()) {
            case CARDIO, HIIT -> PlannedExercise.timed(ex);
            case COMPOUND -> {
                int[] reps = compoundReps(user.getGoal(), athlete);
                yield PlannedExercise.strength(ex, sets(4, profile, true), reps[0], reps[1], 180);
            }
            case ISOLATION -> PlannedExercise.strength(ex, sets(3, profile, false),
                    athlete ? 8 : 10, athlete ? 10 : 15, 90);
        };
    }

    // Fysiske delmål styrer rep-området på de tunge øvelser
    private int[] compoundReps(FitnessGoal goal, boolean athlete) {
        if (goal.getPhysicalGoals().contains("Stærkere")) return new int[]{4, 6};
        if (goal.getPhysicalGoals().contains("Eksplosiv")) return new int[]{3, 5};
        if (goal.getPhysicalGoals().contains("Større")) return new int[]{8, 10};
        return athlete ? new int[]{5, 8} : new int[]{6, 10};
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

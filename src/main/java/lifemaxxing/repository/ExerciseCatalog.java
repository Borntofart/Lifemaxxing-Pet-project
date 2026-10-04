package lifemaxxing.repository;

import lifemaxxing.model.Exercise;
import lifemaxxing.model.Exercise.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Midlertidig liste af øvelser. Bliver skiftet ud med øvelses-databasen senere
public class ExerciseCatalog {

    public static final String PUSH = "PUSH";
    public static final String PULL = "PULL";
    public static final String LEGS = "LEGS";
    public static final String UPPER = "UPPER";
    public static final String LOWER = "LOWER";
    public static final String FULL_A = "FULL_A";
    public static final String FULL_B = "FULL_B";
    public static final String CARDIO = "CARDIO";
    public static final String HIIT = "HIIT";

    public static class Template {
        private final String title;
        private final String category;
        private final List<Exercise> exercises;

        Template(String title, String category, List<Exercise> exercises) {
            this.title = title;
            this.category = category;
            this.exercises = exercises;
        }

        public String getTitle() { return title; }
        public String getCategory() { return category; }
        public List<Exercise> getExercises() { return exercises; }
    }

    private final Map<String, Template> templates = new HashMap<>();

    public ExerciseCatalog() {
        templates.put(PUSH, new Template("Push – Overkrop", "Styrketræning", List.of(
                new Exercise("Bænkpres", Type.COMPOUND),
                new Exercise("Skulderpres", Type.COMPOUND),
                new Exercise("Incline Dumbbell Press", Type.COMPOUND),
                new Exercise("Dips", Type.COMPOUND),
                new Exercise("Sidehævninger", Type.ISOLATION),
                new Exercise("Triceps Pushdown", Type.ISOLATION))));

        templates.put(PULL, new Template("Pull – Ryg og biceps", "Styrketræning", List.of(
                new Exercise("Dødløft", Type.COMPOUND),
                new Exercise("Pull-ups", Type.COMPOUND),
                new Exercise("Bøjet roning", Type.COMPOUND),
                new Exercise("Kabelroning", Type.ISOLATION),
                new Exercise("Face Pulls", Type.ISOLATION),
                new Exercise("Biceps Curls", Type.ISOLATION))));

        templates.put(LEGS, new Template("Ben", "Styrketræning", List.of(
                new Exercise("Squat", Type.COMPOUND),
                new Exercise("Rumænsk dødløft", Type.COMPOUND),
                new Exercise("Benpres", Type.COMPOUND),
                new Exercise("Walking Lunges", Type.COMPOUND),
                new Exercise("Leg Curl", Type.ISOLATION),
                new Exercise("Lægløft", Type.ISOLATION))));

        templates.put(UPPER, new Template("Upper – Overkrop", "Styrketræning", List.of(
                new Exercise("Bænkpres", Type.COMPOUND),
                new Exercise("Bøjet roning", Type.COMPOUND),
                new Exercise("Skulderpres", Type.COMPOUND),
                new Exercise("Pull-ups", Type.COMPOUND),
                new Exercise("Biceps Curls", Type.ISOLATION),
                new Exercise("Triceps Pushdown", Type.ISOLATION))));

        templates.put(LOWER, new Template("Lower – Underkrop", "Styrketræning", List.of(
                new Exercise("Squat", Type.COMPOUND),
                new Exercise("Rumænsk dødløft", Type.COMPOUND),
                new Exercise("Benpres", Type.COMPOUND),
                new Exercise("Bulgarian Split Squat", Type.COMPOUND),
                new Exercise("Leg Curl", Type.ISOLATION),
                new Exercise("Lægløft", Type.ISOLATION))));

        templates.put(FULL_A, new Template("Full Body A", "Styrketræning", List.of(
                new Exercise("Squat", Type.COMPOUND),
                new Exercise("Bænkpres", Type.COMPOUND),
                new Exercise("Bøjet roning", Type.COMPOUND),
                new Exercise("Skulderpres", Type.COMPOUND),
                new Exercise("Biceps Curls", Type.ISOLATION),
                new Exercise("Planke", Type.ISOLATION))));

        templates.put(FULL_B, new Template("Full Body B", "Styrketræning", List.of(
                new Exercise("Dødløft", Type.COMPOUND),
                new Exercise("Incline Dumbbell Press", Type.COMPOUND),
                new Exercise("Pull-ups", Type.COMPOUND),
                new Exercise("Walking Lunges", Type.COMPOUND),
                new Exercise("Face Pulls", Type.ISOLATION),
                new Exercise("Triceps Pushdown", Type.ISOLATION))));

        templates.put(CARDIO, new Template("Cardio – Kondition", "Cardio", List.of(
                new Exercise("Opvarmning, rolig jog", Type.CARDIO, "10 min"),
                new Exercise("Intervalløb", Type.CARDIO, "6 x 2 min"),
                new Exercise("Romaskine", Type.CARDIO, "10 min"),
                new Exercise("Cykel, steady state", Type.CARDIO, "15 min"),
                new Exercise("Nedkøling og stræk", Type.CARDIO, "5 min"))));

        templates.put(HIIT, new Template("HIIT – Circuit", "HIIT", List.of(
                new Exercise("Burpees", Type.HIIT, "4 runder x 40 sek"),
                new Exercise("Kettlebell Swings", Type.HIIT, "4 runder x 40 sek"),
                new Exercise("Mountain Climbers", Type.HIIT, "4 runder x 40 sek"),
                new Exercise("Jump Squats", Type.HIIT, "4 runder x 40 sek"),
                new Exercise("Push-ups", Type.HIIT, "4 runder x 40 sek"),
                new Exercise("Box Jumps", Type.HIIT, "4 runder x 40 sek"))));
    }

    public Template getTemplate(String key) {
        return templates.get(key);
    }
}

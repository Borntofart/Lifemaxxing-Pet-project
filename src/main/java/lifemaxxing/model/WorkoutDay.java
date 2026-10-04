package lifemaxxing.model;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {

    private final String title;
    private final String category;
    private final List<PlannedExercise> exercises;
    private final boolean restDay;

    public WorkoutDay(String title, String category, List<PlannedExercise> exercises) {
        this(title, category, exercises, false);
    }

    private WorkoutDay(String title, String category, List<PlannedExercise> exercises, boolean restDay) {
        this.title = title;
        this.category = category;
        this.exercises = exercises;
        this.restDay = restDay;
    }

    public static WorkoutDay restDay() {
        return new WorkoutDay("Hviledag", "Restitution", new ArrayList<>(), true);
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public List<PlannedExercise> getExercises() { return exercises; }
    public boolean isRestDay() { return restDay; }
}

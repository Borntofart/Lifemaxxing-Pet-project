package lifemaxxing.model;

import java.util.ArrayList;
import java.util.List;

public class WorkoutDay {

    private final int id;
    private final int dayNr;
    private final String title;
    private final String category;
    private final List<PlannedExercise> exercises;
    private final boolean restDay;

    // dayNr er ugedagen, 1 = mandag og 7 = søndag
    public WorkoutDay(int id, int dayNr, String title, List<PlannedExercise> exercises) {
        this(id, dayNr, title, categoryOf(exercises), exercises, false);
    }

    private WorkoutDay(int id, int dayNr, String title, String category, List<PlannedExercise> exercises, boolean restDay) {
        this.id = id;
        this.dayNr = dayNr;
        this.title = title;
        this.category = category;
        this.exercises = exercises;
        this.restDay = restDay;
    }

    public static WorkoutDay restDay(int dayNr) {
        return new WorkoutDay(0, dayNr, "Hviledag", "Restitution", new ArrayList<>(), true);
    }

    private static String categoryOf(List<PlannedExercise> exercises) {
        boolean hiit = false;
        boolean cardio = !exercises.isEmpty();
        for (PlannedExercise ex : exercises) {
            if (ex.getType() == Exercise.Type.HIIT) hiit = true;
            if (ex.getType() != Exercise.Type.CARDIO) cardio = false;
        }
        if (hiit) return "HIIT";
        if (cardio) return "Cardio";
        return "Styrketræning";
    }

    public int getId() { return id; }
    public int getDayNr() { return dayNr; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public List<PlannedExercise> getExercises() { return exercises; }
    public boolean isRestDay() { return restDay; }
}

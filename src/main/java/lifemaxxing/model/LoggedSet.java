package lifemaxxing.model;

// Et sæt brugeren har lavet. weightKg er 0 ved kropsvægtsøvelser
public class LoggedSet {

    private final PlannedExercise plannedExercise;
    private final int setNr;
    private final int reps;
    private final float weightKg;

    public LoggedSet(PlannedExercise plannedExercise, int setNr, int reps, float weightKg) {
        this.plannedExercise = plannedExercise;
        this.setNr = setNr;
        this.reps = reps;
        this.weightKg = weightKg;
    }

    public PlannedExercise getPlannedExercise() { return plannedExercise; }
    public int getSetNr() { return setNr; }
    public int getReps() { return reps; }
    public float getWeightKg() { return weightKg; }
}

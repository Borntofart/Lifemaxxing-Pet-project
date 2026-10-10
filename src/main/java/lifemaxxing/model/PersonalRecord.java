package lifemaxxing.model;

import java.time.LocalDate;
import java.util.Locale;

public class PersonalRecord {

    private final int exerciseId;
    private final String exerciseName;
    private final float weightKg;
    private final int reps;
    private final LocalDate date;

    public PersonalRecord(int exerciseId, String exerciseName, float weightKg, int reps, LocalDate date) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.weightKg = weightKg;
        this.reps = reps;
        this.date = date;
    }

    // Epley formlen. Gør det muligt at sammenligne fx 100 kg x 3 med 90 kg x 6
    public float getEstimatedOneRepMax() {
        if (reps == 1) {
            return weightKg;
        }
        return weightKg * (1 + reps / 30f);
    }

    // Ved kropsvægt tæller reps, ellers den beregnede 1RM
    public boolean isBetterThan(PersonalRecord other) {
        if (other == null) {
            return true;
        }
        if (weightKg > 0 || other.weightKg > 0) {
            return getEstimatedOneRepMax() > other.getEstimatedOneRepMax();
        }
        return reps > other.reps;
    }

    // fx "80 kg x 5" eller "12 reps" ved kropsvægt
    public String getDisplay() {
        if (weightKg <= 0) {
            return reps + " reps";
        }
        String weight = weightKg == Math.round(weightKg)
                ? String.valueOf(Math.round(weightKg))
                : String.format(new Locale("da", "DK"), "%.1f", weightKg);
        return weight + " kg x " + reps;
    }

    public int getExerciseId() { return exerciseId; }
    public String getExerciseName() { return exerciseName; }
    public float getWeightKg() { return weightKg; }
    public int getReps() { return reps; }
    public LocalDate getDate() { return date; }
}

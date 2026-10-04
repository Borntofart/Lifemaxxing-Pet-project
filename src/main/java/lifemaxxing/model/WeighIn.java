package lifemaxxing.model;

import java.time.LocalDate;

public class WeighIn {

    private final LocalDate date;
    private final float weightKg;

    public WeighIn(LocalDate date, float weightKg) {
        this.date = date;
        this.weightKg = weightKg;
    }

    public LocalDate getDate() { return date; }
    public float getWeightKg() { return weightKg; }
}

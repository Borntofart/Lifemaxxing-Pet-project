package lifemaxxing.model;

// En øvelse som den står i brugerens program, fx "Bænkpres" og "4 x 6-8"
public class PlannedExercise {

    private final String name;
    private final String dosage;

    public PlannedExercise(String name, String dosage) {
        this.name = name;
        this.dosage = dosage;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
}

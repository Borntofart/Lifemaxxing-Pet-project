package lifemaxxing.model;

// En øvelse fra kataloget. dosage bruges kun til cardio og HIIT (fx "20 min")
public class Exercise {

    public enum Type {
        COMPOUND, ISOLATION, CARDIO, HIIT
    }

    private final String name;
    private final Type type;
    private final String dosage;

    public Exercise(String name, Type type) {
        this(name, type, null);
    }

    public Exercise(String name, Type type, String dosage) {
        this.name = name;
        this.type = type;
        this.dosage = dosage;
    }

    public String getName() { return name; }
    public Type getType() { return type; }
    public String getDosage() { return dosage; }
}

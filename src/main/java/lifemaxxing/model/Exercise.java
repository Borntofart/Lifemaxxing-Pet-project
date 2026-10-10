package lifemaxxing.model;

// En øvelse fra ovelse-tabellen. dosage bruges kun til cardio og HIIT (fx "20 min")
public class Exercise {

    public enum Type {
        COMPOUND, ISOLATION, CARDIO, HIIT
    }

    private final int id;
    private final String name;
    private final String muscleGroup;
    private final Type type;
    private final String dosage;
    private final String description;

    public Exercise(int id, String name, String muscleGroup, Type type, String dosage, String description) {
        this.id = id;
        this.name = name;
        this.muscleGroup = muscleGroup;
        this.type = type;
        this.dosage = dosage;
        this.description = description;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getMuscleGroup() { return muscleGroup; }
    public Type getType() { return type; }
    public String getDosage() { return dosage; }
    public String getDescription() { return description; }
}

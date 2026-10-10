package lifemaxxing.model;

// En øvelse som den står i brugerens program, fx "Bænkpres" og "4 x 6-8".
// Cardio og HIIT har ingen sæt og reps, kun en dosering som "10 min"
public class PlannedExercise {

    private final int id;
    private final int exerciseId;
    private final String name;
    private final Exercise.Type type;
    private final Integer sets;
    private final Integer repsMin;
    private final Integer repsMax;
    private final Integer restSec;
    private final String timedDosage;

    public PlannedExercise(int id, int exerciseId, String name, Exercise.Type type, Integer sets,
                           Integer repsMin, Integer repsMax, Integer restSec, String timedDosage) {
        this.id = id;
        this.exerciseId = exerciseId;
        this.name = name;
        this.type = type;
        this.sets = sets;
        this.repsMin = repsMin;
        this.repsMax = repsMax;
        this.restSec = restSec;
        this.timedDosage = timedDosage;
    }

    // Før programmet er gemt har øvelsen ikke noget id endnu
    public static PlannedExercise strength(Exercise ex, int sets, int repsMin, int repsMax, int restSec) {
        return new PlannedExercise(0, ex.getId(), ex.getName(), ex.getType(), sets, repsMin, repsMax, restSec, null);
    }

    public static PlannedExercise timed(Exercise ex) {
        return new PlannedExercise(0, ex.getId(), ex.getName(), ex.getType(), null, null, null, null, ex.getDosage());
    }

    public boolean isTimed() {
        return sets == null;
    }

    public String getDosage() {
        if (isTimed()) {
            return timedDosage;
        }
        String reps = repsMax != null && !repsMax.equals(repsMin) ? repsMin + "-" + repsMax : String.valueOf(repsMin);
        return sets + " x " + reps;
    }

    public int getId() { return id; }
    public int getExerciseId() { return exerciseId; }
    public String getName() { return name; }
    public Exercise.Type getType() { return type; }
    public Integer getSets() { return sets; }
    public Integer getRepsMin() { return repsMin; }
    public Integer getRepsMax() { return repsMax; }
    public Integer getRestSec() { return restSec; }
}

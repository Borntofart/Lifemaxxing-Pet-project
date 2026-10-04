package lifemaxxing.model;

public class UserProfile {

    public enum Gender {
        MALE("Mand"),
        FEMALE("Kvinde");

        private final String label;
        Gender(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum ExperienceLevel {
        BEGINNER("Begynder"),
        NOVICE("Let øvet"),
        INTERMEDIATE("Øvet"),
        ADVANCED("Erfaren");

        private final String label;
        ExperienceLevel(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    private int age;
    private Gender gender;
    private float heightCm;
    private float weightKg;
    private ExperienceLevel experienceLevel;
    private String injuryNotes;

    public UserProfile(int age, Gender gender, float heightCm, float weightKg,
                       ExperienceLevel experienceLevel, String injuryNotes) {
        this.age = age;
        this.gender = gender;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.experienceLevel = experienceLevel;
        this.injuryNotes = injuryNotes;
    }

    public float calculateBMI() {
        float heightMeters = heightCm / 100;
        return weightKg / (heightMeters * heightMeters);
    }

    public boolean hasInjury() {
        return injuryNotes != null && !injuryNotes.isBlank();
    }

    public int getAge() { return age; }
    public Gender getGender() { return gender; }
    public float getHeightCm() { return heightCm; }
    public float getWeightKg() { return weightKg; }
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public String getInjuryNotes() { return injuryNotes; }

    public void setAge(int age) { this.age = age; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setHeightCm(float heightCm) { this.heightCm = heightCm; }
    public void setWeightKg(float weightKg) { this.weightKg = weightKg; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public void setInjuryNotes(String injuryNotes) { this.injuryNotes = injuryNotes; }
}

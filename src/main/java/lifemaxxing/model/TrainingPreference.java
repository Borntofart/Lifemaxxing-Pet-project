package lifemaxxing.model;

public class TrainingPreference {

    // Bruges til at vælge hvilke træningsdage programmet bygges af
    public enum SplitType {
        FULL_BODY, UPPER_LOWER, PPL, CARDIO, HIIT, MIX
    }

    private String focus;
    private String trainingStyle;
    private boolean wantsStrength;
    private boolean wantsCardio;
    private int trainingDaysPerWeek;
    private int sessionDurationMin;

    public TrainingPreference(String focus, String trainingStyle, boolean wantsStrength,
                              boolean wantsCardio, int trainingDaysPerWeek, int sessionDurationMin) {
        this.focus = focus;
        this.trainingStyle = trainingStyle;
        this.wantsStrength = wantsStrength;
        this.wantsCardio = wantsCardio;
        this.trainingDaysPerWeek = trainingDaysPerWeek;
        this.sessionDurationMin = sessionDurationMin;
    }

    // Samme logik som det gamle program, tager hensyn til styrke/cardio valgene
    public String resolveGeneratorFocus() {
        if ("HIIT".equals(focus)) return "HIIT";
        if ("Blanding af alt".equals(focus)) return "Blanding af alt";
        if (wantsStrength && wantsCardio) return "Blanding af alt";
        if (wantsCardio) return "Cardio";
        return "Styrke";
    }

    public SplitType getSplitType() {
        if ("HIIT".equals(focus)) return SplitType.HIIT;
        if ("Cardio".equals(focus) && !wantsStrength) return SplitType.CARDIO;
        if ("Blanding af alt".equals(focus) || (wantsStrength && wantsCardio)) return SplitType.MIX;
        if (trainingDaysPerWeek <= 3) return SplitType.FULL_BODY;
        if ("Bodybuilder".equals(trainingStyle) && trainingDaysPerWeek >= 5) return SplitType.PPL;
        return SplitType.UPPER_LOWER;
    }

    public String getRecommendedSplit() {
        int days = trainingDaysPerWeek;
        return switch (getSplitType()) {
            case HIIT -> "HIIT-circuits " + days + " dage/uge (" + sessionDurationMin + " min)";
            case CARDIO -> "Cardio-sessioner " + days + " gange/uge";
            case MIX -> "Mix af styrke og cardio " + days + " dage/uge";
            case FULL_BODY -> "Atlet".equals(trainingStyle)
                    ? "Full Body (squat / bænk / dødløft-fokus)"
                    : "Full Body " + days + "x/uge";
            case PPL -> "Push / Pull / Ben";
            case UPPER_LOWER -> "Atlet".equals(trainingStyle)
                    ? "Upper / Lower med fokus på de store løft"
                    : "Upper / Lower";
        };
    }

    public String getFocus() { return focus; }
    public String getTrainingStyle() { return trainingStyle; }
    public boolean isWantsStrength() { return wantsStrength; }
    public boolean isWantsCardio() { return wantsCardio; }
    public int getTrainingDaysPerWeek() { return trainingDaysPerWeek; }
    public int getSessionDurationMin() { return sessionDurationMin; }
}

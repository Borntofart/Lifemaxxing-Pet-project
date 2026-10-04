package lifemaxxing.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FitnessGoal {

    public enum GoalType {
        BULK_UP("Tage på", "Flere kalorier, fokus på styrke og muskelmasse"),
        LOSE_WEIGHT("Tabe mig", "Kalorieunderskud, behold musklerne og tab fedt"),
        MAINTAIN("Holde vægten", "Stabil vægt, bliv stærkere"),
        RECOMP("Recomp", "Tab fedt og byg muskler på samme tid");

        private final String label;
        private final String description;

        GoalType(String label, String description) {
            this.label = label;
            this.description = description;
        }

        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    private GoalType goalType;
    private float targetWeightKg;
    private float startWeightKg;
    private LocalDate deadline;
    private float weeklyWeightChangeKg;
    private List<String> physicalGoals = new ArrayList<>();

    public FitnessGoal(GoalType goalType, float targetWeightKg, float startWeightKg,
                       LocalDate deadline, float weeklyWeightChangeKg, List<String> physicalGoals) {
        this.goalType = goalType;
        this.targetWeightKg = targetWeightKg;
        this.startWeightKg = startWeightKg;
        this.deadline = deadline;
        this.weeklyWeightChangeKg = weeklyWeightChangeKg;
        setPhysicalGoals(physicalGoals);
    }

    public boolean isGoalReached(float currentWeightKg) {
        if (goalType == GoalType.LOSE_WEIGHT) {
            return currentWeightKg <= targetWeightKg;
        }
        return currentWeightKg >= targetWeightKg;
    }

    // Hvor langt brugeren er nået fra start til mål, 0-100
    public int calculateProgressPercent(float currentWeightKg) {
        float total = startWeightKg - targetWeightKg;

        // Ved vedligehold er start og mål ens, så vi tjekker bare om man er indenfor 1 kg
        if (Math.abs(total) < 0.1f) {
            return Math.abs(currentWeightKg - targetWeightKg) <= 1f ? 100 : 0;
        }

        float done = startWeightKg - currentWeightKg;
        int pct = Math.round(done / total * 100);
        return Math.max(0, Math.min(100, pct));
    }

    public GoalType getGoalType() { return goalType; }
    public float getTargetWeightKg() { return targetWeightKg; }
    public float getStartWeightKg() { return startWeightKg; }
    public LocalDate getDeadline() { return deadline; }
    public float getWeeklyWeightChangeKg() { return weeklyWeightChangeKg; }
    public List<String> getPhysicalGoals() { return physicalGoals; }

    public void setPhysicalGoals(List<String> physicalGoals) {
        this.physicalGoals = physicalGoals != null ? new ArrayList<>(physicalGoals) : new ArrayList<>();
    }
}

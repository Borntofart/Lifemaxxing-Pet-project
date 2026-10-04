package lifemaxxing.model;

public class MacroPlan {

    private int calorieGoal;
    private int proteinGram;
    private int carbohydrateGram;
    private int fatGram;

    // Forklaring på hvordan tallene er regnet ud
    private String calculationMethod;

    public MacroPlan(int calorieGoal, int proteinGram, int carbohydrateGram,
                     int fatGram, String calculationMethod) {
        this.calorieGoal = calorieGoal;
        this.proteinGram = proteinGram;
        this.carbohydrateGram = carbohydrateGram;
        this.fatGram = fatGram;
        this.calculationMethod = calculationMethod;
    }

    // Kulhydrater tager hele justeringen så protein og fedt bliver låst
    public void adjustCalories(int amount) {
        this.calorieGoal += amount;
        this.carbohydrateGram = Math.max(0, carbohydrateGram + amount / 4);
    }

    public String getMacroSplit() {
        int totalKcal = proteinGram * 4 + carbohydrateGram * 4 + fatGram * 9;
        if (totalKcal == 0) {
            return "Ingen makroer sat";
        }
        int proteinPct = proteinGram * 4 * 100 / totalKcal;
        int carbPct = carbohydrateGram * 4 * 100 / totalKcal;
        int fatPct = fatGram * 9 * 100 / totalKcal;
        return "Protein " + proteinPct + "% / Kulhydrat " + carbPct + "% / Fedt " + fatPct + "%";
    }

    public int getCalorieGoal() { return calorieGoal; }
    public int getProteinGram() { return proteinGram; }
    public int getCarbohydrateGram() { return carbohydrateGram; }
    public int getFatGram() { return fatGram; }
    public String getCalculationMethod() { return calculationMethod; }
}

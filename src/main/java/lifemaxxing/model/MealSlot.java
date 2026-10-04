package lifemaxxing.model;

// Et måltid på dagen med sin andel af makroplanen
public class MealSlot {

    private final String name;
    private final int calories;
    private final int proteinGram;
    private final int carbohydrateGram;
    private final int fatGram;

    public MealSlot(String name, int calories, int proteinGram, int carbohydrateGram, int fatGram) {
        this.name = name;
        this.calories = calories;
        this.proteinGram = proteinGram;
        this.carbohydrateGram = carbohydrateGram;
        this.fatGram = fatGram;
    }

    public String getName() { return name; }
    public int getCalories() { return calories; }
    public int getProteinGram() { return proteinGram; }
    public int getCarbohydrateGram() { return carbohydrateGram; }
    public int getFatGram() { return fatGram; }
}

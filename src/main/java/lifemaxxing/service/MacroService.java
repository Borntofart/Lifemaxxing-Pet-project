package lifemaxxing.service;

import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.FitnessGoal.GoalType;
import lifemaxxing.model.MacroPlan;
import lifemaxxing.model.MealSlot;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.UserProfile;

import java.util.List;

// Makro udregner. Mifflin-St Jeor til BMR og ISSN guidelines til protein
public class MacroService {

    public MacroPlan calculateFor(UserProfile profile, FitnessGoal goal, TrainingPreference prefs) {
        float weight = profile.getWeightKg();
        float height = profile.getHeightCm();
        int age = profile.getAge();

        // Trin 1: BMR
        float bmr = 10 * weight + 6.25f * height - 5 * age;
        bmr += profile.getGender() == UserProfile.Gender.MALE ? 5 : -161;

        // Trin 2: aktivitetsfaktor ud fra træningsdage, cardio giver lidt ekstra
        float activityFactor = activityFactor(prefs.getTrainingDaysPerWeek());
        if (prefs.isWantsCardio() && activityFactor < 1.9f) {
            activityFactor = Math.min(1.9f, activityFactor + 0.05f);
        }
        float tdee = bmr * activityFactor;

        // Trin 3: juster kaloreir efter mål
        GoalType goalType = goal.getGoalType();
        float calorieTarget = switch (goalType) {
            case LOSE_WEIGHT -> tdee - 500;
            case BULK_UP -> tdee + 300;
            case RECOMP -> tdee - 100;
            case MAINTAIN -> tdee;
        };

        // Trin 4: protein i g/kg, mere i underskud for at holde på musklerne
        float proteinPerKg = switch (goalType) {
            case LOSE_WEIGHT -> 2.3f;
            case BULK_UP -> 1.8f;
            default -> 1.6f;
        };
        float proteinG = weight * proteinPerKg;

        // Trin 5: fedt, lidt mere til styrke og bodybuilding
        boolean heavy = "Bodybuilder".equals(prefs.getTrainingStyle()) || "Styrke".equals(prefs.getFocus());
        float fatPerKg = heavy ? 1.0f : 0.9f;
        float fatG = weight * fatPerKg;

        // Trin 6: resten bliver til kulhydrater
        float remainingKcal = Math.max(0, calorieTarget - proteinG * 4 - fatG * 9);
        float carbG = remainingKcal / 4f;

        String method = "BMR (Mifflin-St Jeor) " + Math.round(bmr) + " kcal x aktivitet " + activityFactor
                + " = TDEE " + Math.round(tdee) + " kcal. Justeret for målet " + goalType.getLabel().toLowerCase()
                + ". Protein " + proteinPerKg + " g/kg, fedt " + fatPerKg + " g/kg, kulhydrat fylder resten.";

        return new MacroPlan(Math.round(calorieTarget), Math.round(proteinG),
                Math.round(carbG), Math.round(fatG), method);
    }

    // Fordeler dagens makroer på tre måltider
    public List<MealSlot> splitIntoMeals(MacroPlan plan) {
        return List.of(
                meal("Morgenmad", plan, 0.25f),
                meal("Frokost", plan, 0.35f),
                meal("Aftensmad", plan, 0.40f)
        );
    }

    private MealSlot meal(String name, MacroPlan plan, float share) {
        return new MealSlot(name,
                Math.round(plan.getCalorieGoal() * share),
                Math.round(plan.getProteinGram() * share),
                Math.round(plan.getCarbohydrateGram() * share),
                Math.round(plan.getFatGram() * share));
    }

    private float activityFactor(int days) {
        if (days <= 1) return 1.2f;
        if (days <= 3) return 1.375f;
        if (days <= 5) return 1.55f;
        if (days == 6) return 1.725f;
        return 1.9f;
    }
}

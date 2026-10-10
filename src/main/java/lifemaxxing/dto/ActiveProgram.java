package lifemaxxing.dto;

import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.MacroPlan;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.UserProfile.ExperienceLevel;

// Det brugeren har svaret i spørgeskemaet, som det ligger i fitness_program og tabellerne omkring den
public record ActiveProgram(int programId, FitnessGoal goal, TrainingPreference preference,
                            ExperienceLevel experienceLevel, MacroPlan macroPlan) {
}

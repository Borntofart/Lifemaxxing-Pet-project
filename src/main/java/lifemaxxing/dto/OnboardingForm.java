package lifemaxxing.dto;

import lifemaxxing.model.FitnessGoal.GoalType;
import lifemaxxing.model.UserProfile.ExperienceLevel;
import lifemaxxing.model.UserProfile.Gender;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Alle svar fra spørgeskemaet samlet i et objekt
public class OnboardingForm {

    // Profil
    private Integer age;
    private Gender gender;
    private Float heightCm;
    private Float weightKg;
    private ExperienceLevel experienceLevel;
    private String injuryNotes;

    // Mål
    private GoalType goalType;
    private Float targetWeightKg;
    private LocalDate deadline;
    private Float weeklyWeightChangeKg;
    private List<String> physicalGoals = new ArrayList<>();

    // Træningspræferencer
    private String focus = "Styrke";
    private boolean wantsStrength = true;
    private boolean wantsCardio;
    private String trainingStyle = "Bodybuilder";
    private Integer trainingDaysPerWeek = 3;
    private Integer sessionDurationMin = 60;

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public Float getHeightCm() { return heightCm; }
    public void setHeightCm(Float heightCm) { this.heightCm = heightCm; }
    public Float getWeightKg() { return weightKg; }
    public void setWeightKg(Float weightKg) { this.weightKg = weightKg; }
    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }
    public String getInjuryNotes() { return injuryNotes; }
    public void setInjuryNotes(String injuryNotes) { this.injuryNotes = injuryNotes; }

    public GoalType getGoalType() { return goalType; }
    public void setGoalType(GoalType goalType) { this.goalType = goalType; }
    public Float getTargetWeightKg() { return targetWeightKg; }
    public void setTargetWeightKg(Float targetWeightKg) { this.targetWeightKg = targetWeightKg; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public Float getWeeklyWeightChangeKg() { return weeklyWeightChangeKg; }
    public void setWeeklyWeightChangeKg(Float weeklyWeightChangeKg) { this.weeklyWeightChangeKg = weeklyWeightChangeKg; }
    public List<String> getPhysicalGoals() { return physicalGoals; }
    public void setPhysicalGoals(List<String> physicalGoals) { this.physicalGoals = physicalGoals; }

    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }
    public boolean isWantsStrength() { return wantsStrength; }
    public void setWantsStrength(boolean wantsStrength) { this.wantsStrength = wantsStrength; }
    public boolean isWantsCardio() { return wantsCardio; }
    public void setWantsCardio(boolean wantsCardio) { this.wantsCardio = wantsCardio; }
    public String getTrainingStyle() { return trainingStyle; }
    public void setTrainingStyle(String trainingStyle) { this.trainingStyle = trainingStyle; }
    public Integer getTrainingDaysPerWeek() { return trainingDaysPerWeek; }
    public void setTrainingDaysPerWeek(Integer trainingDaysPerWeek) { this.trainingDaysPerWeek = trainingDaysPerWeek; }
    public Integer getSessionDurationMin() { return sessionDurationMin; }
    public void setSessionDurationMin(Integer sessionDurationMin) { this.sessionDurationMin = sessionDurationMin; }
}

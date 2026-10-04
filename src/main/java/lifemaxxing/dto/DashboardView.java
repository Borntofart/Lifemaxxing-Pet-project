package lifemaxxing.dto;

import lifemaxxing.model.MacroPlan;
import lifemaxxing.model.MealSlot;
import lifemaxxing.model.WorkoutDay;

import java.util.List;

// Alt hvad dashboardet viser, færdigformateret så html bare skal udskrive det
public class DashboardView {

    private String username;
    private String initials;

    private String goalTitle;
    private int progressPercent;
    private String startWeight;
    private String currentWeight;
    private String targetWeight;
    private String deadline;

    private WorkoutDay todaysWorkout;
    private String nextWorkoutTitle;
    private String splitText;

    private MacroPlan macroPlan;
    private List<MealSlot> meals;

    private String weightChange;
    private int trainingDays;
    private int sessionLength;
    private String lastWeighIn;

    private boolean injury;
    private String injuryNotes;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
    public String getGoalTitle() { return goalTitle; }
    public void setGoalTitle(String goalTitle) { this.goalTitle = goalTitle; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public String getStartWeight() { return startWeight; }
    public void setStartWeight(String startWeight) { this.startWeight = startWeight; }
    public String getCurrentWeight() { return currentWeight; }
    public void setCurrentWeight(String currentWeight) { this.currentWeight = currentWeight; }
    public String getTargetWeight() { return targetWeight; }
    public void setTargetWeight(String targetWeight) { this.targetWeight = targetWeight; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public WorkoutDay getTodaysWorkout() { return todaysWorkout; }
    public void setTodaysWorkout(WorkoutDay todaysWorkout) { this.todaysWorkout = todaysWorkout; }
    public String getNextWorkoutTitle() { return nextWorkoutTitle; }
    public void setNextWorkoutTitle(String nextWorkoutTitle) { this.nextWorkoutTitle = nextWorkoutTitle; }
    public String getSplitText() { return splitText; }
    public void setSplitText(String splitText) { this.splitText = splitText; }
    public MacroPlan getMacroPlan() { return macroPlan; }
    public void setMacroPlan(MacroPlan macroPlan) { this.macroPlan = macroPlan; }
    public List<MealSlot> getMeals() { return meals; }
    public void setMeals(List<MealSlot> meals) { this.meals = meals; }
    public String getWeightChange() { return weightChange; }
    public void setWeightChange(String weightChange) { this.weightChange = weightChange; }
    public int getTrainingDays() { return trainingDays; }
    public void setTrainingDays(int trainingDays) { this.trainingDays = trainingDays; }
    public int getSessionLength() { return sessionLength; }
    public void setSessionLength(int sessionLength) { this.sessionLength = sessionLength; }
    public String getLastWeighIn() { return lastWeighIn; }
    public void setLastWeighIn(String lastWeighIn) { this.lastWeighIn = lastWeighIn; }
    public boolean isInjury() { return injury; }
    public void setInjury(boolean injury) { this.injury = injury; }
    public String getInjuryNotes() { return injuryNotes; }
    public void setInjuryNotes(String injuryNotes) { this.injuryNotes = injuryNotes; }
}

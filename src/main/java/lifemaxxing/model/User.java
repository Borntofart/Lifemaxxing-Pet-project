package lifemaxxing.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private final UUID id;
    private String username;
    private String email;
    private String password;
    private LocalDate createdAt;

    // Udfyldes efter spørgeskemaet
    private UserProfile profile;
    private FitnessGoal goal;
    private TrainingPreference preference;
    private MacroPlan macroPlan;
    private final List<WeighIn> weighIns = new ArrayList<>();

    public User(String username, String email, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDate.now();
    }

    // Brugern skal igennem spørgeskemaet før dashboardet virker
    public boolean hasCompletedOnboarding() {
        return profile != null && goal != null && preference != null;
    }

    public void addWeighIn(WeighIn weighIn) {
        weighIns.add(weighIn);
    }

    public WeighIn getLatestWeighIn() {
        return weighIns.isEmpty() ? null : weighIns.get(weighIns.size() - 1);
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDate getCreatedAt() { return createdAt; }
    public UserProfile getProfile() { return profile; }
    public FitnessGoal getGoal() { return goal; }
    public TrainingPreference getPreference() { return preference; }
    public MacroPlan getMacroPlan() { return macroPlan; }
    public List<WeighIn> getWeighIns() { return weighIns; }

    public void setProfile(UserProfile profile) { this.profile = profile; }
    public void setGoal(FitnessGoal goal) { this.goal = goal; }
    public void setPreference(TrainingPreference preference) { this.preference = preference; }
    public void setMacroPlan(MacroPlan macroPlan) { this.macroPlan = macroPlan; }
}

package lifemaxxing.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private LocalDate createdAt;

    // Udfyldes efter spørgeskemaet
    private UserProfile profile;
    private FitnessGoal goal;
    private TrainingPreference preference;
    private MacroPlan macroPlan;
    private int activeProgramId;
    private final List<WeighIn> weighIns = new ArrayList<>();

    // Ny bruger der ikke er gemt endnu
    public User(String username, String email, String passwordHash) {
        this(0, username, email, passwordHash, LocalDate.now());
    }

    public User(int id, String username, String email, String passwordHash, LocalDate createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    // Brugern skal igennem spørgeskemaet før dashboardet virker
    public boolean hasCompletedOnboarding() {
        return profile != null && goal != null && preference != null && activeProgramId > 0;
    }

    public void addWeighIn(WeighIn weighIn) {
        weighIns.add(weighIn);
    }

    public WeighIn getLatestWeighIn() {
        return weighIns.isEmpty() ? null : weighIns.get(weighIns.size() - 1);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public LocalDate getCreatedAt() { return createdAt; }
    public UserProfile getProfile() { return profile; }
    public FitnessGoal getGoal() { return goal; }
    public TrainingPreference getPreference() { return preference; }
    public MacroPlan getMacroPlan() { return macroPlan; }
    public int getActiveProgramId() { return activeProgramId; }
    public List<WeighIn> getWeighIns() { return weighIns; }

    public void setId(int id) { this.id = id; }
    public void setProfile(UserProfile profile) { this.profile = profile; }
    public void setGoal(FitnessGoal goal) { this.goal = goal; }
    public void setPreference(TrainingPreference preference) { this.preference = preference; }
    public void setMacroPlan(MacroPlan macroPlan) { this.macroPlan = macroPlan; }
    public void setActiveProgramId(int activeProgramId) { this.activeProgramId = activeProgramId; }
}

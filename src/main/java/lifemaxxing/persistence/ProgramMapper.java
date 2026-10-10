package lifemaxxing.persistence;

import lifemaxxing.dto.ActiveProgram;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.Exercise;
import lifemaxxing.model.FitnessGoal;
import lifemaxxing.model.FitnessGoal.GoalType;
import lifemaxxing.model.MacroPlan;
import lifemaxxing.model.PlannedExercise;
import lifemaxxing.model.TrainingPreference;
import lifemaxxing.model.UserProfile.ExperienceLevel;
import lifemaxxing.model.WorkoutDay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// fitness_program og alt der hænger på den: mål, delmål, præferencer, makroplan og træningsplanen
public class ProgramMapper {

    private final ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(ProgramMapper.class);

    public ProgramMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    // Det gamle program afsluttes i stedet for at blive slettet, så loggede træninger ikke forsvinder.
    // Alt gemmes i en transaktion så der ikke ligger halve programmer i databasen hvis noget fejler
    public int createProgram(int userId, String name, FitnessGoal goal, TrainingPreference prefs,
                             ExperienceLevel experience, MacroPlan macroPlan, List<WorkoutDay> week) throws DatabaseException {
        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                execute(connection, "UPDATE fitness_program SET slut_dato = CURRENT_DATE " +
                        "WHERE bruger_id = ? AND slut_dato IS NULL", userId);

                int goalId = insert(connection,
                        "INSERT INTO fitness_maal (bruger_id, type, beskrivelse, start_dato, slut_dato, " +
                        "start_vaegt_kg, maal_vaegt_kg, ugentlig_aendring_kg) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING maal_id",
                        userId, goal.getGoalType().name(), goal.getGoalType().getLabel(), Date.valueOf(LocalDate.now()),
                        Date.valueOf(goal.getDeadline()), goal.getStartWeightKg(), goal.getTargetWeightKg(),
                        goal.getWeeklyWeightChangeKg());

                for (String subGoal : goal.getPhysicalGoals()) {
                    execute(connection, "INSERT INTO delmaal (maal_id, beskrivelse, deadline) VALUES (?, ?, ?)",
                            goalId, subGoal, Date.valueOf(goal.getDeadline()));
                }

                int prefsId = insert(connection,
                        "INSERT INTO traenings_praeference (bruger_id, dage_pr_uge, minutter_pr_pas, erfaring, fokus, " +
                        "traeningsstil, vil_styrke, vil_cardio) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING praeference_id",
                        userId, prefs.getTrainingDaysPerWeek(), prefs.getSessionDurationMin(), experience.name(),
                        prefs.getFocus(), prefs.getTrainingStyle(), prefs.isWantsStrength(), prefs.isWantsCardio());

                int programId = insert(connection,
                        "INSERT INTO fitness_program (bruger_id, maal_id, praeference_id, navn, start_dato) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING program_id",
                        userId, goalId, prefsId, name, Date.valueOf(LocalDate.now()));

                execute(connection, "INSERT INTO makro_plan (program_id, kalorier, protein_g, kulhydrat_g, fedt_g, beregning) " +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                        programId, macroPlan.getCalorieGoal(), macroPlan.getProteinGram(),
                        macroPlan.getCarbohydrateGram(), macroPlan.getFatGram(), macroPlan.getCalculationMethod());

                int planId = insert(connection, "INSERT INTO traenings_plan (program_id) VALUES (?) RETURNING plan_id", programId);
                int weekId = insert(connection, "INSERT INTO traenings_uge (plan_id, uge_nr) VALUES (?, 1) RETURNING uge_id", planId);

                for (WorkoutDay day : week) {
                    int dayId = insert(connection, "INSERT INTO traenings_dag (uge_id, dag_nr, navn) VALUES (?, ?, ?) RETURNING dag_id",
                            weekId, day.getDayNr(), day.getTitle());
                    saveExercises(connection, dayId, day.getExercises());
                }

                connection.commit();
                return programId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Programmet kunne ikke gemmes");
        }
    }

    public ActiveProgram getActiveProgram(int userId) throws DatabaseException {
        String query = "SELECT p.program_id, m.maal_id, m.type, m.slut_dato, m.start_vaegt_kg, m.maal_vaegt_kg, " +
                       "m.ugentlig_aendring_kg, t.dage_pr_uge, t.minutter_pr_pas, t.erfaring, t.fokus, t.traeningsstil, " +
                       "t.vil_styrke, t.vil_cardio, mp.kalorier, mp.protein_g, mp.kulhydrat_g, mp.fedt_g, mp.beregning " +
                       "FROM fitness_program p " +
                       "JOIN fitness_maal m ON m.maal_id = p.maal_id " +
                       "JOIN traenings_praeference t ON t.praeference_id = p.praeference_id " +
                       "JOIN makro_plan mp ON mp.program_id = p.program_id " +
                       "WHERE p.bruger_id = ? AND p.slut_dato IS NULL " +
                       "ORDER BY p.program_id DESC LIMIT 1";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            try (ResultSet rs = stm.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                List<String> subGoals = getSubGoals(connection, rs.getInt("maal_id"));
                FitnessGoal goal = new FitnessGoal(
                        GoalType.valueOf(rs.getString("type")),
                        rs.getFloat("maal_vaegt_kg"),
                        rs.getFloat("start_vaegt_kg"),
                        rs.getDate("slut_dato").toLocalDate(),
                        rs.getFloat("ugentlig_aendring_kg"),
                        subGoals);
                TrainingPreference prefs = new TrainingPreference(
                        rs.getString("fokus"),
                        rs.getString("traeningsstil"),
                        rs.getBoolean("vil_styrke"),
                        rs.getBoolean("vil_cardio"),
                        rs.getInt("dage_pr_uge"),
                        rs.getInt("minutter_pr_pas"));
                MacroPlan macroPlan = new MacroPlan(
                        rs.getInt("kalorier"),
                        rs.getInt("protein_g"),
                        rs.getInt("kulhydrat_g"),
                        rs.getInt("fedt_g"),
                        rs.getString("beregning"));
                ExperienceLevel experience = ExperienceLevel.valueOf(rs.getString("erfaring"));
                return new ActiveProgram(rs.getInt("program_id"), goal, prefs, experience, macroPlan);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Dit program kunne ikke hentes");
        }
    }

    // Kun træningsdage ligger i databasen. Dage der ikke er med er hviledage
    public List<WorkoutDay> getWeek(int programId) throws DatabaseException {
        String query = "SELECT d.dag_id, d.dag_nr, d.navn, po.planlagt_ovelse_id, po.ovelse_id, o.navn AS ovelse_navn, " +
                       "o.type, o.dosering, po.saet, po.reps_min, po.reps_max, po.hvile_sek " +
                       "FROM traenings_plan tp " +
                       "JOIN traenings_uge u ON u.plan_id = tp.plan_id AND u.uge_nr = 1 " +
                       "JOIN traenings_dag d ON d.uge_id = u.uge_id " +
                       "LEFT JOIN planlagt_ovelse po ON po.dag_id = d.dag_id " +
                       "LEFT JOIN ovelse o ON o.ovelse_id = po.ovelse_id " +
                       "WHERE tp.program_id = ? " +
                       "ORDER BY d.dag_nr, po.raekkefolge";
        List<WorkoutDay> week = new ArrayList<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, programId);
            try (ResultSet rs = stm.executeQuery()) {
                int currentDayId = -1;
                int dayNr = 0;
                String dayName = null;
                List<PlannedExercise> exercises = new ArrayList<>();

                while (rs.next()) {
                    int dayId = rs.getInt("dag_id");
                    if (dayId != currentDayId) {
                        if (currentDayId != -1) {
                            week.add(new WorkoutDay(currentDayId, dayNr, dayName, exercises));
                        }
                        currentDayId = dayId;
                        dayNr = rs.getInt("dag_nr");
                        dayName = rs.getString("navn");
                        exercises = new ArrayList<>();
                    }
                    if (rs.getObject("planlagt_ovelse_id") != null) {
                        exercises.add(new PlannedExercise(
                                rs.getInt("planlagt_ovelse_id"),
                                rs.getInt("ovelse_id"),
                                rs.getString("ovelse_navn"),
                                Exercise.Type.valueOf(rs.getString("type")),
                                rs.getObject("saet", Integer.class),
                                rs.getObject("reps_min", Integer.class),
                                rs.getObject("reps_max", Integer.class),
                                rs.getObject("hvile_sek", Integer.class),
                                rs.getString("dosering")));
                    }
                }
                if (currentDayId != -1) {
                    week.add(new WorkoutDay(currentDayId, dayNr, dayName, exercises));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Træningsugen kunne ikke hentes");
        }
        return week;
    }

    // Gemmer ændringen i makro_justering og opdaterer planen. Returnerer id på justeringen
    public int adjustMacroPlan(int programId, MacroPlan newPlan, int calorieChange, String reason) throws DatabaseException {
        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int adjustmentId = insert(connection,
                        "INSERT INTO makro_justering (makro_plan_id, dato, kalorie_aendring, begrundelse) " +
                        "SELECT makro_plan_id, ?, ?, ? FROM makro_plan WHERE program_id = ? RETURNING justering_id",
                        Date.valueOf(LocalDate.now()), calorieChange, reason, programId);

                execute(connection, "UPDATE makro_plan SET kalorier = ?, protein_g = ?, kulhydrat_g = ?, fedt_g = ?, " +
                        "beregning = ? WHERE program_id = ?",
                        newPlan.getCalorieGoal(), newPlan.getProteinGram(), newPlan.getCarbohydrateGram(),
                        newPlan.getFatGram(), newPlan.getCalculationMethod(), programId);

                connection.commit();
                return adjustmentId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Kostplanen kunne ikke opdateres");
        }
    }

    private void saveExercises(Connection connection, int dayId, List<PlannedExercise> exercises) throws SQLException {
        String query = "INSERT INTO planlagt_ovelse (dag_id, ovelse_id, raekkefolge, saet, reps_min, reps_max, hvile_sek) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stm = connection.prepareStatement(query)) {
            int order = 1;
            for (PlannedExercise ex : exercises) {
                setParams(stm, dayId, ex.getExerciseId(), order++, ex.getSets(), ex.getRepsMin(), ex.getRepsMax(), ex.getRestSec());
                stm.addBatch();
            }
            stm.executeBatch();
        }
    }

    private List<String> getSubGoals(Connection connection, int goalId) throws SQLException {
        List<String> subGoals = new ArrayList<>();
        try (PreparedStatement stm = connection.prepareStatement(
                "SELECT beskrivelse FROM delmaal WHERE maal_id = ? ORDER BY delmaal_id")) {
            stm.setInt(1, goalId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    subGoals.add(rs.getString("beskrivelse"));
                }
            }
        }
        return subGoals;
    }

    // Små hjælpere så transaktionerne ikke drukner i setInt og setString
    private int insert(Connection connection, String query, Object... params) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement(query)) {
            setParams(stm, params);
            try (ResultSet rs = stm.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Ingen id kom tilbage fra: " + query);
                }
                return rs.getInt(1);
            }
        }
    }

    private void execute(Connection connection, String query, Object... params) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement(query)) {
            setParams(stm, params);
            stm.executeUpdate();
        }
    }

    private void setParams(PreparedStatement stm, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stm.setObject(i + 1, params[i]);
        }
    }
}

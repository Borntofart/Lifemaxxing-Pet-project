package lifemaxxing.persistence;

import lifemaxxing.dto.SessionSummary;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.LoggedSet;
import lifemaxxing.model.PersonalRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// traenings_pas, logget_saet og personlig_rekord
public class WorkoutLogMapper {

    private final ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(WorkoutLogMapper.class);

    // Samme formel som PersonalRecord.getEstimatedOneRepMax, bare i SQL så databasen kan sortere efter den
    private static final String ONE_REP_MAX = "(vaegt_kg * (1 + reps / 30.0))";

    public WorkoutLogMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public int saveSession(int dayId, String notes, List<LoggedSet> sets) throws DatabaseException {
        String sessionQuery = "INSERT INTO traenings_pas (dag_id, startet, afsluttet, noter) VALUES (?, ?, ?, ?) RETURNING pas_id";
        String setQuery = "INSERT INTO logget_saet (pas_id, planlagt_ovelse_id, saet_nr, reps, vaegt_kg) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connectionPool.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int sessionId;
                Timestamp now = Timestamp.valueOf(LocalDateTime.now());
                try (PreparedStatement stm = connection.prepareStatement(sessionQuery)) {
                    stm.setInt(1, dayId);
                    stm.setTimestamp(2, now);
                    stm.setTimestamp(3, now);
                    stm.setString(4, notes);
                    try (ResultSet rs = stm.executeQuery()) {
                        rs.next();
                        sessionId = rs.getInt("pas_id");
                    }
                }

                try (PreparedStatement stm = connection.prepareStatement(setQuery)) {
                    for (LoggedSet set : sets) {
                        stm.setInt(1, sessionId);
                        stm.setInt(2, set.getPlannedExercise().getId());
                        stm.setInt(3, set.getSetNr());
                        stm.setInt(4, set.getReps());
                        stm.setFloat(5, set.getWeightKg());
                        stm.addBatch();
                    }
                    stm.executeBatch();
                }

                connection.commit();
                return sessionId;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Træningen kunne ikke gemmes");
        }
    }

    public PersonalRecord getBestRecord(int userId, int exerciseId) throws DatabaseException {
        String query = "SELECT r.ovelse_id, o.navn, r.vaegt_kg, r.reps, r.dato FROM personlig_rekord r " +
                       "JOIN ovelse o USING (ovelse_id) WHERE r.bruger_id = ? AND r.ovelse_id = ? " +
                       "ORDER BY " + ONE_REP_MAX + " DESC, r.reps DESC LIMIT 1";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            stm.setInt(2, exerciseId);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? toRecord(rs) : null;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Rekorden kunne ikke hentes");
        }
    }

    // Der laves en ny række hver gang, så man kan se hvordan rekorden har udviklet sig
    public void saveRecord(int userId, PersonalRecord record) throws DatabaseException {
        String query = "INSERT INTO personlig_rekord (bruger_id, ovelse_id, vaegt_kg, reps, dato) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            stm.setInt(2, record.getExerciseId());
            stm.setFloat(3, record.getWeightKg());
            stm.setInt(4, record.getReps());
            stm.setDate(5, Date.valueOf(record.getDate()));
            stm.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Rekorden kunne ikke gemmes");
        }
    }

    // Den bedste rekord for hver øvelse
    public List<PersonalRecord> getRecords(int userId) throws DatabaseException {
        String query = "SELECT * FROM (" +
                       "SELECT DISTINCT ON (r.ovelse_id) r.ovelse_id, o.navn, r.vaegt_kg, r.reps, r.dato " +
                       "FROM personlig_rekord r JOIN ovelse o USING (ovelse_id) WHERE r.bruger_id = ? " +
                       "ORDER BY r.ovelse_id, " + ONE_REP_MAX + " DESC, r.reps DESC" +
                       ") best ORDER BY dato DESC, navn";
        List<PersonalRecord> records = new ArrayList<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    records.add(toRecord(rs));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Dine rekorder kunne ikke hentes");
        }
        return records;
    }

    public List<SessionSummary> getRecentSessions(int userId, int limit) throws DatabaseException {
        String query = "SELECT pas.startet, d.navn, pas.noter, COUNT(ls.logget_saet_id) AS antal_saet " +
                       "FROM traenings_pas pas " +
                       "JOIN traenings_dag d USING (dag_id) " +
                       "JOIN traenings_uge u USING (uge_id) " +
                       "JOIN traenings_plan tp USING (plan_id) " +
                       "JOIN fitness_program p USING (program_id) " +
                       "LEFT JOIN logget_saet ls USING (pas_id) " +
                       "WHERE p.bruger_id = ? " +
                       "GROUP BY pas.pas_id, pas.startet, d.navn, pas.noter " +
                       "ORDER BY pas.startet DESC LIMIT ?";
        List<SessionSummary> sessions = new ArrayList<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            stm.setInt(2, limit);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    sessions.add(new SessionSummary(
                            rs.getTimestamp("startet").toLocalDateTime(),
                            rs.getString("navn"),
                            rs.getInt("antal_saet"),
                            rs.getString("noter")));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Dine træninger kunne ikke hentes");
        }
        return sessions;
    }

    private PersonalRecord toRecord(ResultSet rs) throws SQLException {
        return new PersonalRecord(
                rs.getInt("ovelse_id"),
                rs.getString("navn"),
                rs.getFloat("vaegt_kg"),
                rs.getInt("reps"),
                rs.getDate("dato").toLocalDate());
    }
}

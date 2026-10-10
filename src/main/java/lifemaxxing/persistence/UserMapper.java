package lifemaxxing.persistence;

import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.User;
import lifemaxxing.model.UserProfile;
import lifemaxxing.model.UserProfile.Gender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;

public class UserMapper {

    private final ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(UserMapper.class);

    public UserMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public User createUser(User user) throws DatabaseException {
        String query = "INSERT INTO bruger (brugernavn, email, password_hash) VALUES (?, ?, ?)";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stm.setString(1, user.getUsername());
            stm.setString(2, user.getEmail());
            stm.setString(3, user.getPasswordHash());
            stm.executeUpdate();
            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt("bruger_id"));
                } else {
                    throw new DatabaseException("Brugeren kunne ikke oprettes");
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Brugeren blev ikke gemt");
        }
        return user;
    }

    public User getUserById(int id) throws DatabaseException {
        String query = "SELECT bruger_id, brugernavn, email, password_hash, oprettet FROM bruger WHERE bruger_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, id);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? toUser(rs) : null;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Brugeren kunne ikke hentes");
        }
    }

    public User getUserByUsername(String username) throws DatabaseException {
        String query = "SELECT bruger_id, brugernavn, email, password_hash, oprettet FROM bruger " +
                       "WHERE LOWER(brugernavn) = LOWER(?)";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setString(1, username);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? toUser(rs) : null;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Søgning efter brugeren fejlede");
        }
    }

    public boolean emailExists(String email) throws DatabaseException {
        String query = "SELECT 1 FROM bruger WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setString(1, email);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Søgning efter email fejlede");
        }
    }

    // Alder gemmes som fødselsdato, så den passer år efter år
    public void saveProfile(User user, UserProfile profile) throws DatabaseException {
        String query = "INSERT INTO bruger_profil (bruger_id, navn, foedselsdato, koen, hoejde_cm, skader) " +
                       "VALUES (?, ?, ?, ?, ?, ?) " +
                       "ON CONFLICT (bruger_id) DO UPDATE SET navn = EXCLUDED.navn, foedselsdato = EXCLUDED.foedselsdato, " +
                       "koen = EXCLUDED.koen, hoejde_cm = EXCLUDED.hoejde_cm, skader = EXCLUDED.skader";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, user.getId());
            stm.setString(2, user.getUsername());
            stm.setDate(3, Date.valueOf(LocalDate.now().minusYears(profile.getAge())));
            stm.setString(4, profile.getGender().name());
            stm.setFloat(5, profile.getHeightCm());
            stm.setString(6, profile.getInjuryNotes());
            stm.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Profilen kunne ikke gemmes");
        }
    }

    // Vægt og erfaring ligger i andre tabeller og sættes af UserService
    public UserProfile getProfile(int userId) throws DatabaseException {
        String query = "SELECT foedselsdato, koen, hoejde_cm, skader FROM bruger_profil WHERE bruger_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            try (ResultSet rs = stm.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int age = Period.between(rs.getDate("foedselsdato").toLocalDate(), LocalDate.now()).getYears();
                Gender gender = Gender.valueOf(rs.getString("koen"));
                float height = rs.getFloat("hoejde_cm");
                String injuries = rs.getString("skader");
                return new UserProfile(age, gender, height, 0, null, injuries == null ? "" : injuries);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Profilen kunne ikke hentes");
        }
    }

    private User toUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("bruger_id");
        String username = rs.getString("brugernavn");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        LocalDate created = rs.getTimestamp("oprettet").toLocalDateTime().toLocalDate();
        return new User(id, username, email, passwordHash, created);
    }
}

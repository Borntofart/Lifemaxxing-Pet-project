package lifemaxxing.persistence;

import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.WeighIn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WeighInMapper {

    private final ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(WeighInMapper.class);

    public WeighInMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    // Der må kun være en vejning om dagen, så en ny vejning samme dag overskriver den gamle
    public int saveWeighIn(int userId, WeighIn weighIn) throws DatabaseException {
        String query = "INSERT INTO vejning (bruger_id, dato, vaegt_kg) VALUES (?, ?, ?) " +
                       "ON CONFLICT (bruger_id, dato) DO UPDATE SET vaegt_kg = EXCLUDED.vaegt_kg " +
                       "RETURNING vejning_id";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            stm.setDate(2, Date.valueOf(weighIn.getDate()));
            stm.setFloat(3, weighIn.getWeightKg());
            try (ResultSet rs = stm.executeQuery()) {
                rs.next();
                return rs.getInt("vejning_id");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Vejningen kunne ikke gemmes");
        }
    }

    // Ældste først, så den sidste i listen er den nyeste
    public List<WeighIn> getWeighIns(int userId) throws DatabaseException {
        String query = "SELECT dato, vaegt_kg FROM vejning WHERE bruger_id = ? ORDER BY dato";
        List<WeighIn> weighIns = new ArrayList<>();
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, userId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    weighIns.add(new WeighIn(rs.getDate("dato").toLocalDate(), rs.getFloat("vaegt_kg")));
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Vejningerne kunne ikke hentes");
        }
        return weighIns;
    }

    public void linkAdjustment(int weighInId, int adjustmentId) throws DatabaseException {
        String query = "UPDATE vejning SET justering_id = ? WHERE vejning_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query)) {
            stm.setInt(1, adjustmentId);
            stm.setInt(2, weighInId);
            stm.executeUpdate();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Vejningen kunne ikke kobles til makrojusteringen");
        }
    }
}

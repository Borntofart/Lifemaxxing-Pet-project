package lifemaxxing.persistence;

import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExerciseMapper {

    private final ConnectionPool connectionPool;
    private static final Logger logger = LoggerFactory.getLogger(ExerciseMapper.class);

    public ExerciseMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    // Sorteret efter id, så den øvelse der står først i en muskelgruppe bliver valgt først
    public List<Exercise> getAllExercises() throws DatabaseException {
        String query = "SELECT ovelse_id, navn, muskelgruppe, type, dosering, beskrivelse " +
                       "FROM ovelse ORDER BY ovelse_id";
        List<Exercise> exercises = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement stm = connection.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                exercises.add(toExercise(rs));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new DatabaseException("Øvelserne kan ikke hentes lige nu");
        }
        return exercises;
    }

    private Exercise toExercise(ResultSet rs) throws SQLException {
        int id = rs.getInt("ovelse_id");
        String name = rs.getString("navn");
        String muscleGroup = rs.getString("muskelgruppe");
        Exercise.Type type = Exercise.Type.valueOf(rs.getString("type"));
        String dosage = rs.getString("dosering");
        String description = rs.getString("beskrivelse");
        return new Exercise(id, name, muscleGroup, type, dosage, description);
    }
}

package lifemaxxing.service;

import lifemaxxing.dto.SessionSummary;
import lifemaxxing.exceptions.DatabaseException;
import lifemaxxing.model.LoggedSet;
import lifemaxxing.model.PersonalRecord;
import lifemaxxing.model.PlannedExercise;
import lifemaxxing.model.User;
import lifemaxxing.model.WorkoutDay;
import lifemaxxing.persistence.WorkoutLogMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Gemmer de træninger brugeren logger og holder styr på personlige rekorder
public class TrainingLogService {

    private final WorkoutLogMapper workoutLogMapper;

    public TrainingLogService(WorkoutLogMapper workoutLogMapper) {
        this.workoutLogMapper = workoutLogMapper;
    }

    // Returnerer en besked for hver ny rekord, så de kan vises til brugeren
    public List<String> logSession(User user, WorkoutDay day, List<LoggedSet> sets, String notes) throws DatabaseException {
        workoutLogMapper.saveSession(day.getId(), notes, sets);

        List<String> newRecords = new ArrayList<>();
        for (PersonalRecord best : bestSetPerExercise(sets).values()) {
            PersonalRecord current = workoutLogMapper.getBestRecord(user.getId(), best.getExerciseId());
            if (best.isBetterThan(current)) {
                workoutLogMapper.saveRecord(user.getId(), best);
                newRecords.add("Ny PR i " + best.getExerciseName() + ": " + best.getDisplay());
            }
        }
        return newRecords;
    }

    public List<PersonalRecord> getRecords(User user) throws DatabaseException {
        return workoutLogMapper.getRecords(user.getId());
    }

    public List<SessionSummary> getRecentSessions(User user) throws DatabaseException {
        return workoutLogMapper.getRecentSessions(user.getId(), 5);
    }

    private Map<Integer, PersonalRecord> bestSetPerExercise(List<LoggedSet> sets) {
        Map<Integer, PersonalRecord> best = new LinkedHashMap<>();
        for (LoggedSet set : sets) {
            PlannedExercise ex = set.getPlannedExercise();
            if (ex.isTimed()) {
                continue;
            }
            PersonalRecord candidate = new PersonalRecord(ex.getExerciseId(), ex.getName(),
                    set.getWeightKg(), set.getReps(), LocalDate.now());
            if (candidate.isBetterThan(best.get(ex.getExerciseId()))) {
                best.put(ex.getExerciseId(), candidate);
            }
        }
        return best;
    }
}

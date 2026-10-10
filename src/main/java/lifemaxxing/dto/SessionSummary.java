package lifemaxxing.dto;

import java.time.LocalDateTime;

// En linje i listen over seneste træninger
public class SessionSummary {

    private final LocalDateTime started;
    private final String dayName;
    private final int setCount;
    private final String notes;

    public SessionSummary(LocalDateTime started, String dayName, int setCount, String notes) {
        this.started = started;
        this.dayName = dayName;
        this.setCount = setCount;
        this.notes = notes;
    }

    public LocalDateTime getStarted() { return started; }
    public String getDayName() { return dayName; }
    public int getSetCount() { return setCount; }
    public String getNotes() { return notes; }
}

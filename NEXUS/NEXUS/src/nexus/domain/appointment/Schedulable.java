package nexus.domain.appointment;

import java.time.LocalDateTime;

/** Interface Segregation: any entity with bookable time slots implements Schedulable. */
public interface Schedulable {
    boolean isAvailableAt(LocalDateTime dateTime, int durationMinutes);
    void bookSlot(LocalDateTime dateTime, int durationMinutes);
    void releaseSlot(LocalDateTime dateTime);
}

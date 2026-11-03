package nexus.domain.appointment;

import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

public record AppointmentCancelledEvent(Appointment appointment, String reason, Instant occurredAt) implements DomainEvent {
    public AppointmentCancelledEvent(Appointment appointment, String reason) { this(appointment, reason, Instant.now()); }
}

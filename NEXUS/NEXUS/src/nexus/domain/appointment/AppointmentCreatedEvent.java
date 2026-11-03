package nexus.domain.appointment;

import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

public record AppointmentCreatedEvent(Appointment appointment, Instant occurredAt) implements DomainEvent {
    public AppointmentCreatedEvent(Appointment appointment) { this(appointment, Instant.now()); }
}

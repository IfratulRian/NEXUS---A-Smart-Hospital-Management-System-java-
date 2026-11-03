package nexus.domain.people.patient;

import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

public record PatientRegisteredEvent(Patient patient, Instant occurredAt) implements DomainEvent {
    public PatientRegisteredEvent(Patient patient) { this(patient, Instant.now()); }
}

package nexus.domain.emergency;

import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

public record EmergencyCreatedEvent(EmergencyCase emergencyCase, Instant occurredAt) implements DomainEvent {
    public EmergencyCreatedEvent(EmergencyCase emergencyCase) {
        this(emergencyCase, Instant.now());
    }
}

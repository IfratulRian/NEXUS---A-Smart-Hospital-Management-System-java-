package nexus.patterns.observer;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
    default String name() { return getClass().getSimpleName(); }
}

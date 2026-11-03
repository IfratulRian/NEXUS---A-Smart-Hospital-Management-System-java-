package nexus.patterns.observer;

@FunctionalInterface
public interface EventListener<E extends DomainEvent> {
    void onEvent(E event);
}

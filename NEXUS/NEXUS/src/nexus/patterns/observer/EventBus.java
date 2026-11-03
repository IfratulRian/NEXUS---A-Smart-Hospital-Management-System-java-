package nexus.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Observer pattern: publishers and listeners only know the event types, not each other. */
public class EventBus {
    private record Subscription<E extends DomainEvent>(Class<E> type, EventListener<E> listener) {
        void deliver(DomainEvent event) {
            if (type.isInstance(event)) listener.onEvent(type.cast(event));
        }
    }

    private final List<Subscription<?>> subscriptions = new CopyOnWriteArrayList<>();

    public <E extends DomainEvent> void subscribe(Class<E> type, EventListener<E> listener) {
        subscriptions.add(new Subscription<>(type, listener));
    }

    public void publish(DomainEvent event) {
        for (Subscription<?> s : subscriptions) {
            try {
                s.deliver(event);
            } catch (RuntimeException e) {
                System.err.println("Listener failed for " + event.name() + ": " + e.getMessage());
            }
        }
    }
}

package nexus.domain.pharmacy;

import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

/** Observer pattern event: fired when medication inventory drops at or below safe reorder thresholds. */
public record MedicineLowStockEvent(Medicine medicine, int currentStock, int minStock, Instant occurredAt) implements DomainEvent {
    public MedicineLowStockEvent(Medicine medicine) {
        this(medicine, medicine.getStockQuantity(), medicine.getMinimumStock(), Instant.now());
    }
}

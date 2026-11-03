package nexus.domain.billing;

import nexus.domain.billing.payment.Payment;
import nexus.patterns.observer.DomainEvent;
import java.time.Instant;

public record PaymentCompletedEvent(Invoice invoice, Payment payment, Instant occurredAt) implements DomainEvent {
    public PaymentCompletedEvent(Invoice invoice, Payment payment) {
        this(invoice, payment, Instant.now());
    }
}

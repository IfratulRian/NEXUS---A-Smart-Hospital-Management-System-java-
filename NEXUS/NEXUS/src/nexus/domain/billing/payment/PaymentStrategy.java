package nexus.domain.billing.payment;

/** Strategy Pattern: Encapsulates distinct payment execution mechanisms. */
public interface PaymentStrategy {
    Payment pay(String paymentId, String invoiceId, double amount);
}

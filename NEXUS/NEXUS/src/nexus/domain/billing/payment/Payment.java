package nexus.domain.billing.payment;

import nexus.domain.common.Identifiable;
import nexus.utility.validation.Validators;
import java.time.Instant;

/** Abstraction & Inheritance: Abstract base class for financial payment transactions. */
public abstract class Payment implements Identifiable<String> {
    private final String id;
    private final String invoiceId;
    private final double amount;
    private final Instant timestamp;
    private final String method;

    protected Payment(String id, String invoiceId, double amount, Instant timestamp, String method) {
        Validators.requireNonNull(id, "Payment id");
        Validators.NOT_BLANK.validate(invoiceId);
        Validators.requireNonNegative(amount, "Payment amount");
        Validators.requireNonNull(timestamp, "Payment timestamp");
        Validators.NOT_BLANK.validate(method);
        this.id = id;
        this.invoiceId = invoiceId.trim();
        this.amount = amount;
        this.timestamp = timestamp;
        this.method = method.trim();
    }

    @Override public String getId() { return id; }
    public String getInvoiceId() { return invoiceId; }
    public double getAmount() { return amount; }
    public Instant getTimestamp() { return timestamp; }
    public String getMethod() { return method; }

    public abstract String getTransactionDetails();

    @Override
    public String toString() {
        return "Payment[" + id + ", " + method + ", $" + String.format("%.2f", amount) + "]";
    }
}

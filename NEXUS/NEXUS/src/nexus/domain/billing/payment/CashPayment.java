package nexus.domain.billing.payment;

import java.time.Instant;

public class CashPayment extends Payment {
    private final double tenderedAmount;
    private final double changeGiven;

    public CashPayment(String id, String invoiceId, double amount, double tenderedAmount) {
        this(id, invoiceId, amount, Instant.now(), tenderedAmount, Math.max(0.0, tenderedAmount - amount));
    }

    public CashPayment(String id, String invoiceId, double amount, Instant timestamp, double tenderedAmount, double changeGiven) {
        super(id, invoiceId, amount, timestamp, "CASH");
        this.tenderedAmount = tenderedAmount;
        this.changeGiven = changeGiven;
    }

    public double getTenderedAmount() { return tenderedAmount; }
    public double getChangeGiven() { return changeGiven; }

    @Override
    public String getTransactionDetails() {
        return "Cash Tendered: $" + String.format("%.2f", tenderedAmount) + ", Change: $" + String.format("%.2f", changeGiven);
    }
}

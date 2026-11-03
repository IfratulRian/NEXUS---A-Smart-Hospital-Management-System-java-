package nexus.domain.billing;

import nexus.domain.billing.payment.Payment;
import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate & Builder Pattern: Invoice aggregates polymorphic Billable charges and Payment transactions,
 * enforcing mathematical consistency between subtotal, discount, tax, payments, and balance.
 */
public class Invoice implements Identifiable<String> {
    private final String id;
    private final String patientId;
    private final LocalDate issueDate;
    private final List<Billable> charges = new ArrayList<>();
    private double discountAmount;
    private double taxAmount;
    private double paidAmount;
    private InvoiceStatus status;
    private final List<Payment> payments = new ArrayList<>();

    private Invoice(Builder b) {
        Validators.requireNonNull(b.id, "Invoice id");
        Validators.NOT_BLANK.validate(b.patientId);
        Validators.requireNonNull(b.issueDate, "Issue date");
        this.id = b.id;
        this.patientId = b.patientId.trim();
        this.issueDate = b.issueDate;
        this.charges.addAll(b.charges);
        this.discountAmount = b.discountAmount;
        this.taxAmount = b.taxAmount;
        this.paidAmount = b.paidAmount;
        this.status = b.status;
        this.payments.addAll(b.payments);
    }

    @Override public String getId() { return id; }
    public String getPatientId() { return patientId; }
    public LocalDate getIssueDate() { return issueDate; }
    public List<Billable> getCharges() { return Collections.unmodifiableList(charges); }
    public double getDiscountAmount() { return discountAmount; }
    public double getTaxAmount() { return taxAmount; }
    public double getPaidAmount() { return paidAmount; }
    public InvoiceStatus getStatus() { return status; }
    public List<Payment> getPayments() { return Collections.unmodifiableList(payments); }

    public double getSubtotal() {
        return charges.stream().mapToDouble(Billable::getAmount).sum();
    }

    public double getTotalAmount() {
        return Math.max(0.0, getSubtotal() - discountAmount + taxAmount);
    }

    public double getBalanceDue() {
        return Math.max(0.0, getTotalAmount() - paidAmount);
    }

    public boolean isPaid() { return getBalanceDue() <= 0.001 && getTotalAmount() > 0; }

    public void addCharge(Billable charge) {
        Validators.requireNonNull(charge, "Charge");
        this.charges.add(charge);
        updateStatus();
    }

    public void recordPayment(Payment payment) {
        Validators.requireNonNull(payment, "Payment");
        if (payment.getAmount() <= 0) throw new ValidationException("Payment amount must be greater than zero");
        this.payments.add(payment);
        this.paidAmount += payment.getAmount();
        updateStatus();
    }

    private void updateStatus() {
        double due = getBalanceDue();
        if (due <= 0.001) {
            this.status = InvoiceStatus.PAID;
        } else if (this.paidAmount > 0) {
            this.status = InvoiceStatus.PARTIALLY_PAID;
        } else {
            this.status = InvoiceStatus.UNPAID;
        }
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String id;
        private String patientId;
        private LocalDate issueDate = LocalDate.now();
        private final List<Billable> charges = new ArrayList<>();
        private double discountAmount = 0.0;
        private double taxAmount = 0.0;
        private double paidAmount = 0.0;
        private InvoiceStatus status = InvoiceStatus.UNPAID;
        private final List<Payment> payments = new ArrayList<>();

        private Builder() { }
        public Builder id(String id) { this.id = id; return this; }
        public Builder patientId(String patientId) { this.patientId = patientId; return this; }
        public Builder issueDate(LocalDate issueDate) { this.issueDate = issueDate; return this; }
        public Builder charge(Billable charge) { if (charge != null) this.charges.add(charge); return this; }
        public Builder charges(List<Billable> charges) { if (charges != null) this.charges.addAll(charges); return this; }
        public Builder discount(double discount) { this.discountAmount = discount; return this; }
        public Builder tax(double tax) { this.taxAmount = tax; return this; }
        public Builder paid(double paid) { this.paidAmount = paid; return this; }
        public Builder status(InvoiceStatus status) { this.status = status; return this; }
        public Builder payment(Payment p) { if (p != null) this.payments.add(p); return this; }

        public Invoice build() {
            Invoice inv = new Invoice(this);
            inv.updateStatus();
            return inv;
        }
    }
}

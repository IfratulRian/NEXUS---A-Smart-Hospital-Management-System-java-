package nexus.domain.insurance;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;

public class Claim implements Identifiable<String> {
    private final String id;
    private final String policyNumber;
    private final String patientId;
    private final String invoiceId;
    private final double claimAmount;
    private double approvedAmount;
    private ClaimStatus status;
    private String notes;

    public Claim(String id, String policyNumber, String patientId, String invoiceId, double claimAmount) {
        this(id, policyNumber, patientId, invoiceId, claimAmount, 0.0, ClaimStatus.SUBMITTED, "Submitted for review");
    }

    public Claim(String id, String policyNumber, String patientId, String invoiceId,
                 double claimAmount, double approvedAmount, ClaimStatus status, String notes) {
        Validators.requireNonNull(id, "Claim id");
        Validators.NOT_BLANK.validate(policyNumber);
        Validators.NOT_BLANK.validate(patientId);
        Validators.NOT_BLANK.validate(invoiceId);
        Validators.requireNonNegative(claimAmount, "Claim amount");
        Validators.requireNonNull(status, "Claim status");
        this.id = id;
        this.policyNumber = policyNumber.trim().toUpperCase();
        this.patientId = patientId.trim().toUpperCase();
        this.invoiceId = invoiceId.trim().toUpperCase();
        this.claimAmount = claimAmount;
        this.approvedAmount = approvedAmount;
        this.status = status;
        this.notes = notes == null ? "" : notes.trim();
    }

    @Override public String getId() { return id; }
    public String getPolicyNumber() { return policyNumber; }
    public String getPatientId() { return patientId; }
    public String getInvoiceId() { return invoiceId; }
    public double getClaimAmount() { return claimAmount; }
    public double getApprovedAmount() { return approvedAmount; }
    public ClaimStatus getStatus() { return status; }
    public String getNotes() { return notes; }

    public void transitionTo(ClaimStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new ValidationException("Cannot transition claim from " + status + " to " + next);
        }
        this.status = next;
    }

    public void review() {
        transitionTo(ClaimStatus.UNDER_REVIEW);
        this.notes = "Claim is currently under adjudicator review";
    }

    public void approve(double approvedAmount) {
        if (approvedAmount <= 0 || approvedAmount > claimAmount) {
            throw new ValidationException("Approved amount ($" + approvedAmount + ") must be > 0 and <= claim amount ($" + claimAmount + ")");
        }
        transitionTo(ClaimStatus.APPROVED);
        this.approvedAmount = approvedAmount;
        this.notes = "Approved for $" + String.format("%.2f", approvedAmount);
    }

    public void reject(String reason) {
        transitionTo(ClaimStatus.REJECTED);
        this.notes = (reason == null || reason.isBlank()) ? "Claim rejected" : reason.trim();
    }

    public void markPaid() {
        transitionTo(ClaimStatus.PAID);
        this.notes = "Claim settlement disbursed";
    }

    @Override
    public String toString() {
        return "Claim[" + id + ", policy=" + policyNumber + ", amount=$" + String.format("%.2f", claimAmount) + ", status=" + status + "]";
    }
}

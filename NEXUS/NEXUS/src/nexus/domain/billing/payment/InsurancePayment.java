package nexus.domain.billing.payment;

import java.time.Instant;

public class InsurancePayment extends Payment {
    private final String policyNumber;
    private final String claimId;

    public InsurancePayment(String id, String invoiceId, double amount, String policyNumber, String claimId) {
        this(id, invoiceId, amount, Instant.now(), policyNumber, claimId);
    }

    public InsurancePayment(String id, String invoiceId, double amount, Instant timestamp, String policyNumber, String claimId) {
        super(id, invoiceId, amount, timestamp, "INSURANCE");
        this.policyNumber = policyNumber == null ? "" : policyNumber.trim();
        this.claimId = claimId == null ? "" : claimId.trim();
    }

    public String getPolicyNumber() { return policyNumber; }
    public String getClaimId() { return claimId; }

    @Override
    public String getTransactionDetails() {
        return "Insurance Policy: " + policyNumber + " (Claim: " + claimId + ")";
    }
}

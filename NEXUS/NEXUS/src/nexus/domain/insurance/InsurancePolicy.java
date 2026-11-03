package nexus.domain.insurance;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

public class InsurancePolicy implements Identifiable<String> {
    private final String policyNumber;
    private final String providerName;
    private final String patientId;
    private final double coveragePercent;
    private final double maxCoverage;
    private double usedAmount;
    private final LocalDate expiryDate;

    public InsurancePolicy(String policyNumber, String providerName, String patientId,
                           double coveragePercent, double maxCoverage, LocalDate expiryDate) {
        this(policyNumber, providerName, patientId, coveragePercent, maxCoverage, 0.0, expiryDate);
    }

    public InsurancePolicy(String policyNumber, String providerName, String patientId,
                           double coveragePercent, double maxCoverage, double usedAmount, LocalDate expiryDate) {
        Validators.requireNonNull(policyNumber, "Policy number");
        Validators.NOT_BLANK.validate(providerName);
        Validators.NOT_BLANK.validate(patientId);
        Validators.requireNonNegative(coveragePercent, "Coverage percentage");
        Validators.requireNonNegative(maxCoverage, "Maximum coverage");
        Validators.requireNonNull(expiryDate, "Expiry date");
        this.policyNumber = policyNumber.trim().toUpperCase();
        this.providerName = providerName.trim();
        this.patientId = patientId.trim().toUpperCase();
        this.coveragePercent = coveragePercent;
        this.maxCoverage = maxCoverage;
        this.usedAmount = usedAmount;
        this.expiryDate = expiryDate;
    }

    @Override public String getId() { return policyNumber; }
    public String getPolicyNumber() { return policyNumber; }
    public String getProviderName() { return providerName; }
    public String getPatientId() { return patientId; }
    public double getCoveragePercent() { return coveragePercent; }
    public double getMaxCoverage() { return maxCoverage; }
    public double getUsedAmount() { return usedAmount; }
    public LocalDate getExpiryDate() { return expiryDate; }

    public double getRemainingCoverage() { return Math.max(0.0, maxCoverage - usedAmount); }
    public boolean isExpired() { return expiryDate.isBefore(LocalDate.now()); }

    public double calculateEligibleCoverage(double claimAmount) {
        if (isExpired()) return 0.0;
        double proposed = claimAmount * (coveragePercent / 100.0);
        return Math.min(proposed, getRemainingCoverage());
    }

    public void deduct(double approvedAmount) {
        if (approvedAmount > getRemainingCoverage() + 0.001) {
            throw new ValidationException("Approved amount exceeds remaining coverage ($" + String.format("%.2f", getRemainingCoverage()) + ")");
        }
        this.usedAmount += approvedAmount;
    }

    @Override
    public String toString() {
        return "Policy[" + policyNumber + ", provider=" + providerName + ", coverage=" + coveragePercent + "%, remaining=$" + String.format("%.2f", getRemainingCoverage()) + "]";
    }
}

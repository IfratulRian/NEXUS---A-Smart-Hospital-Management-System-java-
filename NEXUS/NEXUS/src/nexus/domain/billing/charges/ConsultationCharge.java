package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record ConsultationCharge(String doctorId, String doctorName, double fee) implements Billable {
    @Override public double getAmount() { return fee; }
    @Override public String getDescription() { return "Consultation with Dr. " + doctorName + " (" + doctorId + ")"; }
    @Override public String getCategory() { return "CONSULTATION"; }
}

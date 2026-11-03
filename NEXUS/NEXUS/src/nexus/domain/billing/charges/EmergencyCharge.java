package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record EmergencyCharge(String emergencyCaseId, String triageLevel, double baseFee) implements Billable {
    @Override public double getAmount() { return baseFee; }
    @Override public String getDescription() { return "Emergency Trauma Care (" + triageLevel + ", Case " + emergencyCaseId + ")"; }
    @Override public String getCategory() { return "EMERGENCY"; }
}

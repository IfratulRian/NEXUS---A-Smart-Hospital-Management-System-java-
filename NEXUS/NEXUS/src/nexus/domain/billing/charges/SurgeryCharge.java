package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record SurgeryCharge(String procedureName, String leadSurgeon, double surgeonFee, double otFee) implements Billable {
    @Override public double getAmount() { return surgeonFee + otFee; }
    @Override public String getDescription() { return "Surgical Procedure: " + procedureName + " (Surgeon: " + leadSurgeon + ", OT fee: $" + String.format("%.2f", otFee) + ")"; }
    @Override public String getCategory() { return "SURGERY"; }
}

package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record LaboratoryCharge(String testName, String category, double fee) implements Billable {
    @Override public double getAmount() { return fee; }
    @Override public String getDescription() { return "Diagnostic Lab Test: " + testName + " (" + category + ")"; }
    @Override public String getCategory() { return "LABORATORY"; }
}

package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record RoomCharge(String roomOrBedId, String roomType, int numberOfDays, double dailyRate) implements Billable {
    @Override public double getAmount() { return numberOfDays * dailyRate; }
    @Override public String getDescription() { return "Room / Bed Accommodation (" + roomType + ", " + numberOfDays + " days @ $" + String.format("%.2f", dailyRate) + "/day)"; }
    @Override public String getCategory() { return "ACCOMMODATION"; }
}

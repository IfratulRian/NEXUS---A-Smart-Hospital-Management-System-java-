package nexus.domain.billing.charges;

import nexus.domain.billing.Billable;

public record MedicineCharge(String medicineId, String medicineName, int quantity, double unitPrice, double total) implements Billable {
    public MedicineCharge(String medicineId, String medicineName, int quantity, double unitPrice) {
        this(medicineId, medicineName, quantity, unitPrice, quantity * unitPrice);
    }
    @Override public double getAmount() { return total; }
    @Override public String getDescription() { return "Medication: " + medicineName + " (" + quantity + " x BDT " + String.format("%.2f", unitPrice) + ")"; }
    @Override public String getCategory() { return "PHARMACY"; }
}

package nexus.domain.pharmacy;

import java.time.LocalDate;

public class Capsule extends Medicine {
    private final int capsulesPerBox;

    public Capsule(String id, String name, String genericName, String supplier,
                   String batchNumber, LocalDate expiryDate, double unitPrice,
                   int stockQuantity, int minimumStock, int capsulesPerBox) {
        super(id, name, genericName, supplier, batchNumber, expiryDate, unitPrice, stockQuantity, minimumStock);
        this.capsulesPerBox = capsulesPerBox > 0 ? capsulesPerBox : 30;
    }

    public int getCapsulesPerBox() { return capsulesPerBox; }

    @Override
    public double calculatePrice(int quantity) {
        double raw = quantity * getUnitPrice();
        // 8% discount if purchasing full box(es)
        if (quantity >= capsulesPerBox && quantity % capsulesPerBox == 0) {
            return raw * 0.92;
        }
        return raw;
    }

    @Override public String getDosageForm() { return "Capsule (Enteric-coated)"; }
    @Override public String getAdministrationRoute() { return "Oral"; }
}

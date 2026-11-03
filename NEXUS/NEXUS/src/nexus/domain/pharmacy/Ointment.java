package nexus.domain.pharmacy;

import java.time.LocalDate;

public class Ointment extends Medicine {
    private final int tubeWeightGrams;

    public Ointment(String id, String name, String genericName, String supplier,
                    String batchNumber, LocalDate expiryDate, double unitPrice,
                    int stockQuantity, int minimumStock, int tubeWeightGrams) {
        super(id, name, genericName, supplier, batchNumber, expiryDate, unitPrice, stockQuantity, minimumStock);
        this.tubeWeightGrams = tubeWeightGrams > 0 ? tubeWeightGrams : 20;
    }

    public int getTubeWeightGrams() { return tubeWeightGrams; }

    @Override
    public double calculatePrice(int quantity) {
        return quantity * getUnitPrice();
    }

    @Override public String getDosageForm() { return "Topical Ointment (" + tubeWeightGrams + "g tube)"; }
    @Override public String getAdministrationRoute() { return "Topical"; }
}

package nexus.domain.pharmacy;

import java.time.LocalDate;

public class Syrup extends Medicine {
    private final int bottleVolumeMl;

    public Syrup(String id, String name, String genericName, String supplier,
                 String batchNumber, LocalDate expiryDate, double unitPrice,
                 int stockQuantity, int minimumStock, int bottleVolumeMl) {
        super(id, name, genericName, supplier, batchNumber, expiryDate, unitPrice, stockQuantity, minimumStock);
        this.bottleVolumeMl = bottleVolumeMl > 0 ? bottleVolumeMl : 100;
    }

    public int getBottleVolumeMl() { return bottleVolumeMl; }

    @Override
    public double calculatePrice(int quantity) {
        return quantity * getUnitPrice();
    }

    @Override public String getDosageForm() { return "Syrup (" + bottleVolumeMl + "ml bottle)"; }
    @Override public String getAdministrationRoute() { return "Oral Liquid"; }
}

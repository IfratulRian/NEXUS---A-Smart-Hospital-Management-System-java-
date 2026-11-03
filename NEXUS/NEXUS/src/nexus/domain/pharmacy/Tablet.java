package nexus.domain.pharmacy;

import java.time.LocalDate;

public class Tablet extends Medicine {
    private final int tabletsPerStrip;

    public Tablet(String id, String name, String genericName, String supplier,
                  String batchNumber, LocalDate expiryDate, double unitPrice,
                  int stockQuantity, int minimumStock, int tabletsPerStrip) {
        super(id, name, genericName, supplier, batchNumber, expiryDate, unitPrice, stockQuantity, minimumStock);
        this.tabletsPerStrip = tabletsPerStrip > 0 ? tabletsPerStrip : 10;
    }

    public int getTabletsPerStrip() { return tabletsPerStrip; }

    @Override
    public double calculatePrice(int quantity) {
        double raw = quantity * getUnitPrice();
        // 5% discount if purchasing full strip(s)
        if (quantity >= tabletsPerStrip && quantity % tabletsPerStrip == 0) {
            return raw * 0.95;
        }
        return raw;
    }

    @Override public String getDosageForm() { return "Tablet"; }
    @Override public String getAdministrationRoute() { return "Oral"; }
}

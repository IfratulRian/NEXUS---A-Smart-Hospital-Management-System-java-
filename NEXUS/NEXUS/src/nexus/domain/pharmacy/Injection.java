package nexus.domain.pharmacy;

import java.time.LocalDate;

public class Injection extends Medicine {
    private final int dosageMg;
    private final boolean requiresRefrigeration;

    public Injection(String id, String name, String genericName, String supplier,
                     String batchNumber, LocalDate expiryDate, double unitPrice,
                     int stockQuantity, int minimumStock, int dosageMg, boolean requiresRefrigeration) {
        super(id, name, genericName, supplier, batchNumber, expiryDate, unitPrice, stockQuantity, minimumStock);
        this.dosageMg = dosageMg;
        this.requiresRefrigeration = requiresRefrigeration;
    }

    public int getDosageMg() { return dosageMg; }
    public boolean requiresRefrigeration() { return requiresRefrigeration; }

    @Override
    public double calculatePrice(int quantity) {
        // Includes sterile handling fee of 15.0 per vial
        return (quantity * getUnitPrice()) + (quantity * 15.0);
    }

    @Override public String getDosageForm() { return "Injection Vial (" + dosageMg + "mg)"; }
    @Override public String getAdministrationRoute() { return "IV / IM"; }
}

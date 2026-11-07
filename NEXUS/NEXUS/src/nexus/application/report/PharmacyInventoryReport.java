package nexus.application.report;

import nexus.application.pharmacy.PharmacyService;
import nexus.domain.pharmacy.Medicine;
import java.util.List;

public class PharmacyInventoryReport implements Report {
    private final PharmacyService service;

    public PharmacyInventoryReport(PharmacyService service) {
        this.service = service;
    }

    @Override public String getTitle() { return "Pharmacy Inventory & Stock Valuation"; }

    @Override
    public String generate() {
        List<Medicine> all = service.listAll();
        List<Medicine> lowStock = service.listLowStock();
        List<Medicine> expiring = service.listExpiringSoon(30);

        int totalUnits = all.stream().mapToInt(Medicine::getStockQuantity).sum();
        double totalValuation = all.stream().mapToDouble(m -> m.getStockQuantity() * m.getUnitPrice()).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Total Registered Drug Products: ").append(all.size()).append("\n");
        sb.append("Total Inventory Units on Hand: ").append(totalUnits).append("\n");
        sb.append(String.format("Total Inventory Valuation: BDT %.2f\n\n", totalValuation));

        sb.append("--- Low Stock Alerts (At or below threshold) ---\n");
        if (lowStock.isEmpty()) sb.append("  All stock levels healthy.\n");
        else for (Medicine m : lowStock) {
            sb.append(String.format("  ! %s [%s] - Current: %d, Min: %d (Supplier: %s)\n",
                    m.getName(), m.getId(), m.getStockQuantity(), m.getMinimumStock(), m.getSupplier()));
        }

        sb.append("\n--- Expiring Within 30 Days ---\n");
        if (expiring.isEmpty()) sb.append("  No medications expiring within 30 days.\n");
        else for (Medicine m : expiring) {
            sb.append(String.format("  ! %s [%s] - Expiry: %s (Batch: %s, Stock: %d)\n",
                    m.getName(), m.getId(), m.getExpiryDate(), m.getBatchNumber(), m.getStockQuantity()));
        }
        return sb.toString();
    }
}

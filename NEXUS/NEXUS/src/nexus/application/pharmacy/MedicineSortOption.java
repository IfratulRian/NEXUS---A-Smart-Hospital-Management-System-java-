package nexus.application.pharmacy;

import nexus.domain.pharmacy.Medicine;
import java.util.Comparator;

/** Strategy pattern via enum: encapsulated comparators for pharmaceutical sorting. */
public enum MedicineSortOption {
    BY_NAME("Name", Comparator.comparing(m -> m.getName().toLowerCase())),
    BY_PRICE_LOW("Price (lowest first)", Comparator.comparingDouble(Medicine::getUnitPrice)),
    BY_PRICE_HIGH("Price (highest first)", Comparator.comparingDouble(Medicine::getUnitPrice).reversed()),
    BY_STOCK("Stock quantity (lowest first)", Comparator.comparingInt(Medicine::getStockQuantity)),
    BY_EXPIRY("Expiry date (soonest first)", Comparator.comparing(Medicine::getExpiryDate));

    private final String label;
    private final Comparator<Medicine> comparator;

    MedicineSortOption(String label, Comparator<Medicine> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public Comparator<Medicine> comparator() { return comparator; }
    @Override public String toString() { return label; }
}

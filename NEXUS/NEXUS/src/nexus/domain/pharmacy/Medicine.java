package nexus.domain.pharmacy;

import nexus.domain.common.Identifiable;
import nexus.exception.ValidationException;
import nexus.utility.validation.Validators;
import java.time.LocalDate;

/** Abstraction & Encapsulation: Abstract base class for pharmaceuticals with stock & expiry management. */
public abstract class Medicine implements Identifiable<String> {
    private final String id;
    private final String name;
    private final String genericName;
    private final String supplier;
    private final String batchNumber;
    private final LocalDate expiryDate;
    private double unitPrice;
    private int stockQuantity;
    private final int minimumStock;

    protected Medicine(String id, String name, String genericName, String supplier,
                       String batchNumber, LocalDate expiryDate, double unitPrice,
                       int stockQuantity, int minimumStock) {
        Validators.requireNonNull(id, "Medicine id");
        Validators.NOT_BLANK.validate(name);
        Validators.NOT_BLANK.validate(genericName);
        Validators.NOT_BLANK.validate(batchNumber);
        Validators.requireNonNull(expiryDate, "Expiry date");
        Validators.requireNonNegative(unitPrice, "Unit price");
        if (stockQuantity < 0) throw new ValidationException("Stock cannot be negative");
        if (minimumStock < 0) throw new ValidationException("Minimum stock cannot be negative");
        this.id = id;
        this.name = name.trim();
        this.genericName = genericName.trim();
        this.supplier = supplier == null ? "" : supplier.trim();
        this.batchNumber = batchNumber.trim();
        this.expiryDate = expiryDate;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.minimumStock = minimumStock;
    }

    @Override public String getId() { return id; }
    public String getName() { return name; }
    public String getGenericName() { return genericName; }
    public String getSupplier() { return supplier; }
    public String getBatchNumber() { return batchNumber; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public double getUnitPrice() { return unitPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public int getMinimumStock() { return minimumStock; }

    public void setUnitPrice(double unitPrice) {
        Validators.requireNonNegative(unitPrice, "Unit price");
        this.unitPrice = unitPrice;
    }

    public void restock(int quantity) {
        if (quantity <= 0) throw new ValidationException("Restock quantity must be positive");
        this.stockQuantity += quantity;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) throw new ValidationException("Dispense quantity must be positive");
        if (quantity > this.stockQuantity) {
            throw new ValidationException("Insufficient stock for " + name + " (available: " + stockQuantity + ", requested: " + quantity + ")");
        }
        this.stockQuantity -= quantity;
    }

    public boolean isLowStock() { return this.stockQuantity <= this.minimumStock; }
    public boolean isExpired() { return this.expiryDate.isBefore(LocalDate.now()); }

    /** Polymorphic calculation: each form of medication applies its packaging/discount rules. */
    public abstract double calculatePrice(int quantity);
    public abstract String getDosageForm();
    public abstract String getAdministrationRoute();
}

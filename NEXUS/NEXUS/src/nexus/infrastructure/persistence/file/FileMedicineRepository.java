package nexus.infrastructure.persistence.file;

import nexus.domain.pharmacy.Capsule;
import nexus.domain.pharmacy.Injection;
import nexus.domain.pharmacy.Medicine;
import nexus.domain.pharmacy.Ointment;
import nexus.domain.pharmacy.Syrup;
import nexus.domain.pharmacy.Tablet;
import nexus.infrastructure.persistence.repository.MedicineRepository;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class FileMedicineRepository extends FileRepository<Medicine> implements MedicineRepository {
    public FileMedicineRepository(Path file) { super(file); }

    @Override
    public List<Medicine> findByNameContaining(String fragment) {
        String needle = fragment.toLowerCase();
        return findAll().stream()
                .filter(m -> m.getName().toLowerCase().contains(needle) || m.getGenericName().toLowerCase().contains(needle))
                .toList();
    }

    @Override
    public List<Medicine> findLowStock() {
        return findAll().stream().filter(Medicine::isLowStock).toList();
    }

    @Override
    public List<Medicine> findExpiringBefore(LocalDate date) {
        return findAll().stream().filter(m -> m.getExpiryDate().isBefore(date)).toList();
    }

    @Override
    protected String serialize(Medicine m) {
        String type;
        String extra1 = "";
        String extra2 = "";

        if (m instanceof Tablet t) {
            type = "TABLET";
            extra1 = String.valueOf(t.getTabletsPerStrip());
        } else if (m instanceof Capsule c) {
            type = "CAPSULE";
            extra1 = String.valueOf(c.getCapsulesPerBox());
        } else if (m instanceof Syrup s) {
            type = "SYRUP";
            extra1 = String.valueOf(s.getBottleVolumeMl());
        } else if (m instanceof Injection i) {
            type = "INJECTION";
            extra1 = String.valueOf(i.getDosageMg());
            extra2 = String.valueOf(i.requiresRefrigeration());
        } else if (m instanceof Ointment o) {
            type = "OINTMENT";
            extra1 = String.valueOf(o.getTubeWeightGrams());
        } else {
            type = "TABLET";
        }

        return String.join("\t",
                type,
                m.getId(),
                clean(m.getName()),
                clean(m.getGenericName()),
                clean(m.getSupplier()),
                clean(m.getBatchNumber()),
                m.getExpiryDate().toString(),
                String.valueOf(m.getUnitPrice()),
                String.valueOf(m.getStockQuantity()),
                String.valueOf(m.getMinimumStock()),
                extra1,
                extra2);
    }

    @Override
    protected Medicine deserialize(String[] f) {
        String type = f[0].toUpperCase();
        String id = f[1];
        String name = f[2];
        String generic = f[3];
        String supplier = f[4];
        String batch = f[5];
        LocalDate expiry = LocalDate.parse(f[6]);
        double price = Double.parseDouble(f[7]);
        int stock = Integer.parseInt(f[8]);
        int minStock = Integer.parseInt(f[9]);

        return switch (type) {
            case "CAPSULE" -> new Capsule(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    f.length > 10 && !f[10].isBlank() ? Integer.parseInt(f[10]) : 30);
            case "SYRUP" -> new Syrup(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    f.length > 10 && !f[10].isBlank() ? Integer.parseInt(f[10]) : 100);
            case "INJECTION" -> new Injection(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    f.length > 10 && !f[10].isBlank() ? Integer.parseInt(f[10]) : 500,
                    f.length > 11 && Boolean.parseBoolean(f[11]));
            case "OINTMENT" -> new Ointment(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    f.length > 10 && !f[10].isBlank() ? Integer.parseInt(f[10]) : 20);
            default -> new Tablet(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    f.length > 10 && !f[10].isBlank() ? Integer.parseInt(f[10]) : 10);
        };
    }
}

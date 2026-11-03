package nexus.presentation.console.menu;

import nexus.application.medical.MedicalRecordService;
import nexus.application.pharmacy.MedicineSortOption;
import nexus.application.pharmacy.PharmacyService;
import nexus.domain.pharmacy.Capsule;
import nexus.domain.pharmacy.Injection;
import nexus.domain.pharmacy.Medicine;
import nexus.domain.pharmacy.Ointment;
import nexus.domain.pharmacy.Syrup;
import nexus.domain.pharmacy.Tablet;
import nexus.presentation.console.input.ConsoleInput;
import nexus.presentation.console.output.ConsoleOutput;
import nexus.security.authentication.SessionContext;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.utility.id.IdGenerator;
import nexus.utility.validation.Validators;
import java.time.LocalDate;
import java.util.List;

public class PharmacyMenu extends Menu {
    private final PharmacyService pharmacyService;
    private final MedicalRecordService medicalRecordService;
    private final IdGenerator medicineIdGen;

    public PharmacyMenu(PharmacyService pharmacyService, MedicalRecordService medicalRecordService,
                        IdGenerator medicineIdGen, ConsoleInput in, ConsoleOutput out,
                        SessionContext session, AuthorizationService authorization) {
        super("PHARMACY & INVENTORY MANAGEMENT", in, out, session, authorization);
        this.pharmacyService = pharmacyService;
        this.medicalRecordService = medicalRecordService;
        this.medicineIdGen = medicineIdGen;
    }

    @Override
    protected void registerItems() {
        add("Add new medicine", Permission.INVENTORY_MANAGE, this::addMedicine);
        add("List all medicines & stock", Permission.PRESCRIPTION_VIEW, this::listSorted);
        add("Search medicine by name", Permission.PRESCRIPTION_VIEW, this::searchMedicine);
        add("Dispense medicine", Permission.MEDICINE_DISPENSE, this::dispenseMedicine);
        add("Dispense by prescription ID", Permission.MEDICINE_DISPENSE, this::dispenseByPrescription);
        add("Restock existing medicine", Permission.INVENTORY_MANAGE, this::restockMedicine);
        add("View low stock inventory", Permission.INVENTORY_MANAGE, () -> printTable(pharmacyService.listLowStock()));
        add("View expiring medicines", Permission.INVENTORY_MANAGE, this::expiringSoon);
    }

    private void listSorted() {
        MedicineSortOption sort = in.readEnum("Sort medicines by", MedicineSortOption.class);
        printTable(pharmacyService.listAll(sort));
    }

    private void addMedicine() {
        out.println("Select medicine type:");
        out.println("  1. Tablet");
        out.println("  2. Capsule");
        out.println("  3. Syrup");
        out.println("  4. Injection");
        out.println("  5. Ointment");
        int typeChoice = in.readInt("Type", 1, 5);

        String id = medicineIdGen.next();
        String name = in.readValidated("Brand / Trade name", Validators.NOT_BLANK);
        String generic = in.readValidated("Generic chemical name", Validators.NOT_BLANK);
        String supplier = in.readLine("Supplier / Manufacturer");
        String batch = in.readValidated("Batch number", Validators.NOT_BLANK);
        LocalDate expiry = in.readDate("Expiry date");
        double price = in.readNonNegativeDouble("Unit price");
        int stock = in.readInt("Initial stock quantity", 0, 100000);
        int minStock = in.readInt("Minimum stock alert threshold", 1, 10000);

        Medicine med = switch (typeChoice) {
            case 1 -> new Tablet(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    in.readInt("Tablets per strip", 1, 50));
            case 2 -> new Capsule(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    in.readInt("Capsules per box", 1, 100));
            case 3 -> new Syrup(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    in.readInt("Bottle volume in ml", 10, 1000));
            case 4 -> new Injection(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    in.readInt("Dosage in mg", 1, 5000),
                    in.readLine("Requires refrigeration? (y/n)").equalsIgnoreCase("y"));
            case 5 -> new Ointment(id, name, generic, supplier, batch, expiry, price, stock, minStock,
                    in.readInt("Tube weight in grams", 5, 200));
            default -> throw new IllegalStateException();
        };

        pharmacyService.addMedicine(med);
        out.success(med.getDosageForm() + " " + med.getName() + " registered with ID " + med.getId());
    }

    private void searchMedicine() {
        String query = in.readLine("Search term");
        printTable(pharmacyService.search(query));
    }

    private void dispenseMedicine() {
        String id = in.readLine("Medicine ID (e.g. MED-00001)");
        int qty = in.readInt("Quantity to dispense", 1, 1000);
        double charged = pharmacyService.dispense(id, qty);
        out.success("Dispensed " + qty + " units. Total price: $" + String.format("%.2f", charged));
    }

    private void dispenseByPrescription() {
        String prxId = in.readLine("Prescription ID (e.g. PRX-00001)");
        double total = pharmacyService.dispenseByPrescription(prxId, medicalRecordService);
        out.success("Prescription " + prxId + " dispensed successfully. Total medication cost: $" + String.format("%.2f", total));
    }

    private void restockMedicine() {
        String id = in.readLine("Medicine ID");
        int qty = in.readInt("Restock quantity", 1, 50000);
        pharmacyService.restock(id, qty);
        out.success("Restocked " + qty + " units.");
    }

    private void expiringSoon() {
        int days = in.readInt("Expiring within days ahead", 1, 365);
        printTable(pharmacyService.listExpiringSoon(days));
    }

    private void printTable(List<Medicine> meds) {
        if (meds.isEmpty()) { out.info("No medicines found."); return; }
        out.table(List.of("ID", "Name", "Form", "Stock", "Min", "Price", "Expiry"),
                meds.stream().map(m -> List.of(
                        m.getId(), m.getName(), m.getDosageForm(),
                        String.valueOf(m.getStockQuantity()), String.valueOf(m.getMinimumStock()),
                        String.format("%.2f", m.getUnitPrice()), m.getExpiryDate().toString())).toList());
    }
}

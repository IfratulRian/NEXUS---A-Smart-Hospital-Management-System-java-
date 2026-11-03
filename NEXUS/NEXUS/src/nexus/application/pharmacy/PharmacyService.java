package nexus.application.pharmacy;

import nexus.application.medical.MedicalRecordService;
import nexus.domain.medical.Prescription;
import nexus.domain.medical.PrescriptionItem;
import nexus.domain.pharmacy.Medicine;
import nexus.domain.pharmacy.MedicineLowStockEvent;
import nexus.exception.EntityNotFoundException;
import nexus.infrastructure.logging.AuditAction;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.repository.MedicineRepository;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.SessionContext;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Pharmacy domain service coordinating drug inventory, dispensing, and scheduled background checks. */
public class PharmacyService {
    private final MedicineRepository repository;
    private final EventBus eventBus;
    private final AuditLogger audit;
    private final SessionContext session;
    private final ScheduledExecutorService scheduler;

    public PharmacyService(MedicineRepository repository, EventBus eventBus, AuditLogger audit, SessionContext session) {
        this(repository, eventBus, audit, session, true);
    }

    public PharmacyService(MedicineRepository repository, EventBus eventBus, AuditLogger audit, SessionContext session, boolean startScheduler) {
        this.repository = repository;
        this.eventBus = eventBus;
        this.audit = audit;
        this.session = session;
        if (startScheduler) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "nexus-pharmacy-stock-checker");
                t.setDaemon(true);
                return t;
            });
            this.scheduler.scheduleAtFixedRate(this::checkInventoryAlerts, 1, 60, TimeUnit.MINUTES);
        } else {
            this.scheduler = null;
        }
    }

    public void addMedicine(Medicine medicine) {
        repository.save(medicine);
    }

    public Medicine getMedicine(String id) {
        return repository.findById(id.trim().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException("Medicine " + id + " not found"));
    }

    public List<Medicine> listAll() {
        return listAll(MedicineSortOption.BY_NAME);
    }

    public List<Medicine> listAll(MedicineSortOption sortOption) {
        return repository.findAll().stream().sorted(sortOption.comparator()).toList();
    }

    public List<Medicine> search(String query) {
        return repository.findByNameContaining(query);
    }

    public List<Medicine> listLowStock() {
        return repository.findLowStock();
    }

    public List<Medicine> listExpiringSoon(int daysAhead) {
        return repository.findExpiringBefore(LocalDate.now().plusDays(daysAhead));
    }

    public void restock(String medicineId, int quantity) {
        Medicine m = getMedicine(medicineId);
        m.restock(quantity);
        repository.save(m);
    }

    public double dispense(String medicineId, int quantity) {
        Medicine m = getMedicine(medicineId);
        double totalPrice = m.calculatePrice(quantity);
        m.reduceStock(quantity);
        repository.save(m);

        audit.log(AuditEntry.of(session.currentUsername(), AuditAction.MEDICINE_DISPENSED, "Medicine", m.getId(), "SUCCESS"));

        if (m.isLowStock()) {
            eventBus.publish(new MedicineLowStockEvent(m));
        }
        return totalPrice;
    }

    public double dispenseByPrescription(String prescriptionId, MedicalRecordService medicalRecordService) {
        Prescription prx = medicalRecordService.getPrescription(prescriptionId);
        if (prx.isDispensed()) {
            throw new IllegalStateException("Prescription " + prescriptionId + " is already dispensed");
        }
        double totalCost = 0.0;
        for (PrescriptionItem item : prx.getItems()) {
            List<Medicine> matches = repository.findByNameContaining(item.medicineName());
            if (!matches.isEmpty()) {
                Medicine med = matches.get(0);
                totalCost += dispense(med.getId(), item.durationDays() > 0 ? item.durationDays() : 1);
            }
        }
        medicalRecordService.markDispensed(prescriptionId);
        return totalCost;
    }

    public void checkInventoryAlerts() {
        for (Medicine m : repository.findLowStock()) {
            eventBus.publish(new MedicineLowStockEvent(m));
        }
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}

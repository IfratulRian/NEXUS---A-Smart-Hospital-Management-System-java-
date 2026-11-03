package nexus;

import nexus.application.appointment.AppointmentRequest;
import nexus.application.appointment.AppointmentService;
import nexus.application.patient.PatientService;
import nexus.domain.appointment.*;
import nexus.domain.billing.Invoice;
import nexus.domain.billing.charges.ConsultationCharge;
import nexus.domain.billing.charges.MedicineCharge;
import nexus.domain.billing.payment.CashPayment;
import nexus.domain.billing.payment.PaymentFactory;
import nexus.domain.billing.payment.PaymentStrategy;
import nexus.domain.common.BloodGroup;
import nexus.domain.common.DepartmentType;
import nexus.domain.common.Gender;
import nexus.domain.emergency.EmergencyCase;
import nexus.domain.emergency.EmergencyStatus;
import nexus.domain.emergency.TriageLevel;
import nexus.domain.emergency.TriageQueue;
import nexus.domain.hospital.*;
import nexus.domain.insurance.Claim;
import nexus.domain.insurance.ClaimStatus;
import nexus.domain.insurance.InsurancePolicy;
import nexus.domain.people.doctor.Doctor;
import nexus.domain.people.patient.*;
import nexus.domain.pharmacy.Capsule;
import nexus.domain.pharmacy.Injection;
import nexus.domain.pharmacy.Medicine;
import nexus.domain.pharmacy.Tablet;
import nexus.exception.*;
import nexus.infrastructure.logging.AuditEntry;
import nexus.infrastructure.logging.AuditLogger;
import nexus.infrastructure.persistence.file.FileAppointmentRepository;
import nexus.infrastructure.persistence.file.FileDoctorRepository;
import nexus.infrastructure.persistence.file.FilePatientRepository;
import nexus.patterns.factory.UserFactory;
import nexus.patterns.observer.EventBus;
import nexus.security.authentication.*;
import nexus.security.authorization.AuthorizationService;
import nexus.security.role.Permission;
import nexus.security.role.Role;
import nexus.utility.id.SequentialIdGenerator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/** Dependency-free unit & integration test runner for NEXUS. */
public class NexusTests {
    private static int passed, failed;

    public static void main(String[] args) throws Exception {
        run("ID generator formats and seeds", NexusTests::idGenerator);
        run("Patient builder validates input", NexusTests::patientValidation);
        run("Patient status transitions", NexusTests::statusTransitions);
        run("Duplicate patient rejected", NexusTests::duplicatePatient);
        run("Repository round trip persists patient", NexusTests::repositoryRoundTrip);
        run("Password hashing is salted and verifiable", NexusTests::passwordHashing);
        run("Authorization follows role permissions", NexusTests::authorization);
        run("Event bus delivers to matching listeners", NexusTests::eventBus);

        // Extended domain & service tests
        run("Doctor Schedulable detects time conflict", NexusTests::appointmentConflicts);
        run("Appointment lifecycle state transitions", NexusTests::appointmentStateTransitions);
        run("Emergency PriorityQueue orders by triage severity", NexusTests::emergencyPriorityOrdering);
        run("Emergency case state transitions", NexusTests::emergencyStateTransitions);
        run("Medicine hierarchy polymorphic pricing", NexusTests::medicinePolymorphism);
        run("Medicine stock reduction and low stock check", NexusTests::medicineStockControl);
        run("Invoice charges, subtotal, discount, and balance due", NexusTests::invoiceCalculations);
        run("Payment strategies execute polymorphically", NexusTests::paymentStrategies);
        run("Insurance policy coverage and deductible calculation", NexusTests::insuranceCoverage);
        run("Insurance claim state transition invariants", NexusTests::claimTransitions);
        run("Hospital department bed allocation and release", NexusTests::hospitalBedAllocation);
        run("User factory and account status enforcement", NexusTests::userAccountManagement);

        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }

    private static void idGenerator() {
        SequentialIdGenerator gen = SequentialIdGenerator.seededFrom("PAT", List.of("PAT-00007", "PAT-00003", "DOC-00099"));
        check("PAT-00008".equals(gen.next()), "should continue after highest PAT id");
    }

    private static void patientValidation() {
        expect(InvalidPatientException.class, () -> sample().email("not-an-email").build());
        expect(InvalidPatientException.class, () -> sample().phone("abc").build());
        expect(InvalidPatientException.class, () -> sample().dateOfBirth(LocalDate.now().plusDays(1)).build());
        expect(InvalidPatientException.class, () -> sample().bloodGroup(null).build());
        check(sample().build().getAge() >= 40, "valid patient builds");
    }

    private static void statusTransitions() {
        Patient p = sample().build();
        p.transitionTo(PatientStatus.ADMITTED);
        p.transitionTo(PatientStatus.DISCHARGED);
        expect(InvalidPatientException.class, () -> p.transitionTo(PatientStatus.REGISTERED));
    }

    private static void duplicatePatient() throws Exception {
        PatientService service = newPatientService(Files.createTempDirectory("nexus-test"));
        service.registerPatient(sample());
        expect(DuplicateEntityException.class, () -> service.registerPatient(sample()));
    }

    private static void repositoryRoundTrip() throws Exception {
        Path file = Files.createTempDirectory("nexus-test").resolve("patients.txt");
        FilePatientRepository repo = new FilePatientRepository(file);
        repo.save(sample().id("PAT-00001").build());
        Patient loaded = new FilePatientRepository(file).findById("PAT-00001").orElseThrow();
        check("John Doe".equals(loaded.getName()) && loaded.getBloodGroup() == BloodGroup.O_POSITIVE, "fields preserved");
        check(loaded.getAllergies().equals(List.of("penicillin")), "allergies preserved");
    }

    private static void passwordHashing() {
        PasswordHasher h = new Sha256PasswordHasher();
        String a = h.hash("secret1"), b = h.hash("secret1");
        check(!a.equals(b), "salts differ");
        check(h.matches("secret1", a) && !h.matches("wrong", a), "verification");
    }

    private static void authorization() {
        AuthorizationService authz = new AuthorizationService();
        User doctor = new User("doc", "x", Role.DOCTOR, UserStatus.ACTIVE);
        User admin = new User("adm", "x", Role.ADMIN, UserStatus.ACTIVE);
        check(authz.isAllowed(admin, Permission.USER_MANAGE), "admin can do everything");
        check(!authz.isAllowed(doctor, Permission.INVENTORY_MANAGE), "doctor cannot manage inventory");
        expect(AuthorizationException.class, () -> authz.require(doctor, Permission.BILLING_MANAGE));
    }

    private static void eventBus() {
        EventBus bus = new EventBus();
        AtomicInteger count = new AtomicInteger();
        bus.subscribe(PatientRegisteredEvent.class, e -> count.incrementAndGet());
        bus.publish(new PatientRegisteredEvent(sample().id("PAT-00001").build()));
        check(count.get() == 1, "listener called once");
    }

    private static void appointmentConflicts() throws Exception {
        Path dir = Files.createTempDirectory("nexus-app-test");
        PatientService patSvc = newPatientService(dir);
        Patient p1 = patSvc.registerPatient(sample());
        Patient p2 = patSvc.registerPatient(sample().phone("01799999999").email("p2@example.com"));

        FileDoctorRepository docRepo = new FileDoctorRepository(dir.resolve("docs.txt"));
        Doctor doc = new Doctor("DOC-00001", "Dr. House", LocalDate.of(1975, 1, 1), Gender.MALE,
                "01711112222", "house@nexus.com", DepartmentType.CARDIOLOGY, LocalDate.now(),
                "Cardiologist", "MD", 20, 1000.0);
        docRepo.save(doc);

        AppointmentService appSvc = new AppointmentService(new FileAppointmentRepository(dir.resolve("app.txt")),
                docRepo, patSvc, new SequentialIdGenerator("APP", 0), new EventBus(), noopAudit(), new SessionContext());

        LocalDateTime slot = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Appointment a1 = appSvc.scheduleAppointment(new AppointmentRequest(p1.getId(), doc.getId(), slot, 30, AppointmentPriority.ROUTINE, "Checkup"));
        check(a1.getStatus() == AppointmentStatus.SCHEDULED, "appointment scheduled");

        // Overlapping slot must throw ConflictException
        expect(ConflictException.class, () ->
                appSvc.scheduleAppointment(new AppointmentRequest(p2.getId(), doc.getId(), slot.plusMinutes(15), 30, AppointmentPriority.URGENT, "Conflict Check")));
    }

    private static void appointmentStateTransitions() {
        Appointment a = new Appointment("APP-00001", "PAT-00001", "DOC-00001",
                LocalDateTime.now().plusDays(1), 30, AppointmentPriority.ROUTINE, "Consultation");
        check(a.getStatus() == AppointmentStatus.SCHEDULED, "initial status is SCHEDULED");
        a.confirm();
        check(a.getStatus() == AppointmentStatus.CONFIRMED, "status after confirm");
        a.start();
        check(a.getStatus() == AppointmentStatus.IN_PROGRESS, "status after start");
        a.complete("Patient stable");
        check(a.getStatus() == AppointmentStatus.COMPLETED, "status after complete");
        expect(ValidationException.class, () -> a.cancel("Too late"));
    }

    private static void emergencyPriorityOrdering() {
        TriageQueue queue = new TriageQueue();
        EmergencyCase low = new EmergencyCase("EMG-01", "Patient Low", null, TriageLevel.LOW, "Rash", null);
        EmergencyCase urgent = new EmergencyCase("EMG-02", "Patient Urgent", null, TriageLevel.URGENT, "Fracture", null);
        EmergencyCase critical = new EmergencyCase("EMG-03", "Patient Critical", null, TriageLevel.CRITICAL, "Cardiac Arrest", null);

        queue.enqueue(low);
        queue.enqueue(urgent);
        queue.enqueue(critical);

        check(queue.pollNext().orElseThrow().getTriageLevel() == TriageLevel.CRITICAL, "Critical must be first");
        check(queue.pollNext().orElseThrow().getTriageLevel() == TriageLevel.URGENT, "Urgent must be second");
        check(queue.pollNext().orElseThrow().getTriageLevel() == TriageLevel.LOW, "Low must be third");
    }

    private static void emergencyStateTransitions() {
        EmergencyCase ec = new EmergencyCase("EMG-01", "Trauma Victim", null, TriageLevel.CRITICAL, "Severe Trauma", null);
        check(ec.getStatus() == EmergencyStatus.TRIAGED, "initial TRIAGED");
        ec.assignDoctor("DOC-00001");
        check(ec.getStatus() == EmergencyStatus.ATTENDING, "transitioned to ATTENDING");
        ec.transitionTo(EmergencyStatus.STABILIZED);
        ec.transitionTo(EmergencyStatus.DISCHARGED);
        expect(ValidationException.class, () -> ec.transitionTo(EmergencyStatus.ATTENDING));
    }

    private static void medicinePolymorphism() {
        Tablet tab = new Tablet("MED-01", "Paracetamol", "Acetaminophen", "Beximco", "B101",
                LocalDate.now().plusYears(1), 2.0, 100, 10, 10);
        check(tab.calculatePrice(10) == 19.0, "10 tablets (1 strip) gets 5% discount: 20 * 0.95 = 19.0");
        check(tab.calculatePrice(5) == 10.0, "5 tablets gets regular pricing: 5 * 2.0 = 10.0");

        Injection inj = new Injection("MED-02", "Ceftriaxone", "Cephalosporin", "Square", "B102",
                LocalDate.now().plusYears(1), 100.0, 20, 5, 1000, true);
        check(inj.calculatePrice(2) == 230.0, "Injection includes $15 sterile fee per vial: 2 * 100 + 2 * 15 = 230.0");
    }

    private static void medicineStockControl() {
        Capsule cap = new Capsule("MED-03", "Omeprazole", "PPI", "Incepta", "B103",
                LocalDate.now().plusYears(1), 5.0, 15, 10, 30);
        check(!cap.isLowStock(), "15 > minStock 10");
        cap.reduceStock(6);
        check(cap.getStockQuantity() == 9, "remaining stock is 9");
        check(cap.isLowStock(), "9 <= minStock 10 triggers low stock");
        expect(ValidationException.class, () -> cap.reduceStock(100));
    }

    private static void invoiceCalculations() {
        Invoice inv = Invoice.builder()
                .id("INV-00001")
                .patientId("PAT-00001")
                .charge(new ConsultationCharge("DOC-01", "Dr. Watson", 500.0))
                .charge(new MedicineCharge("MED-01", "Paracetamol", 2, 50.0))
                .discount(50.0)
                .tax(25.0)
                .build();

        check(inv.getSubtotal() == 600.0, "subtotal 500 + 100 = 600");
        check(inv.getTotalAmount() == 575.0, "total 600 - 50 + 25 = 575");
        check(inv.getBalanceDue() == 575.0, "initial balance due = 575");

        inv.recordPayment(new CashPayment("PAY-01", inv.getId(), 200.0, 200.0));
        check(inv.getPaidAmount() == 200.0, "paid amount 200");
        check(inv.getBalanceDue() == 375.0, "balance due 375");
        check(!inv.isPaid(), "not fully paid yet");
    }

    private static void paymentStrategies() {
        PaymentStrategy cashStrat = PaymentFactory.cash(500.0);
        CashPayment cp = (CashPayment) cashStrat.pay("PAY-01", "INV-01", 450.0);
        check(cp.getChangeGiven() == 50.0, "change given is 50.0");

        PaymentStrategy cardStrat = PaymentFactory.card("1234", "AUTH-999");
        check("CARD".equals(cardStrat.pay("PAY-02", "INV-01", 100.0).getMethod()), "card payment method");
    }

    private static void insuranceCoverage() {
        InsurancePolicy policy = new InsurancePolicy("POL-01", "Delta Health", "PAT-00001",
                80.0, 1000.0, LocalDate.now().plusMonths(6));
        double cov = policy.calculateEligibleCoverage(500.0);
        check(cov == 400.0, "80% of 500 = 400.0");
        policy.deduct(400.0);
        check(policy.getRemainingCoverage() == 600.0, "remaining coverage 1000 - 400 = 600.0");
    }

    private static void claimTransitions() {
        Claim claim = new Claim("CLM-01", "POL-01", "PAT-01", "INV-01", 800.0);
        check(claim.getStatus() == ClaimStatus.SUBMITTED, "initial SUBMITTED");
        claim.review();
        check(claim.getStatus() == ClaimStatus.UNDER_REVIEW, "UNDER_REVIEW");
        claim.approve(640.0);
        check(claim.getStatus() == ClaimStatus.APPROVED, "APPROVED");
        claim.markPaid();
        check(claim.getStatus() == ClaimStatus.PAID, "PAID");
        expect(ValidationException.class, () -> claim.reject("Cannot reject settled claim"));
    }

    private static void hospitalBedAllocation() {
        Hospital hosp = new Hospital("HOSP-01", "NEXUS");
        Department cardio = new Department("DEP-CARDIO", DepartmentType.CARDIOLOGY, "Cardiology");
        Ward ward = new Ward("WRD-01", "Cardio Ward 1", DepartmentType.CARDIOLOGY);
        Room room = new Room("RM-01", "101", RoomType.GENERAL_WARD);
        Bed b1 = new Bed("BED-01", "B1", BedType.GENERAL);
        room.addBed(b1);
        ward.addRoom(room);
        cardio.addWard(ward);
        hosp.addDepartment(cardio);

        check(hosp.getTotalBeds() == 1, "1 bed total");
        Optional<Bed> alloc = hosp.allocateBed(DepartmentType.CARDIOLOGY, "PAT-00001");
        check(alloc.isPresent() && alloc.get().isOccupied(), "bed allocated and marked occupied");
        check(hosp.getOccupiedBeds() == 1, "1 bed occupied");

        alloc.get().release();
        check(!alloc.get().isOccupied(), "bed released");
        check(hosp.getOccupiedBeds() == 0, "0 beds occupied");
    }

    private static void userAccountManagement() {
        PasswordHasher hasher = new Sha256PasswordHasher();
        UserFactory factory = new UserFactory(hasher);
        User user = factory.create("john_nurse", "pass1234", Role.NURSE, "NUR-00001");
        check(user.getStaffId().orElse("").equals("NUR-00001"), "staff id linked");
        check(user.isActive(), "new user active");
        user.setStatus(UserStatus.DISABLED);
        check(!user.isActive(), "user can be disabled");
    }

    // ---- helpers ----
    private static Patient.Builder sample() {
        return Patient.builder().name("John Doe").dateOfBirth(LocalDate.of(1980, 5, 10)).gender(Gender.MALE)
                .phone("01712345678").email("john@example.com").bloodGroup(BloodGroup.O_POSITIVE)
                .emergencyContact(new EmergencyContact("Jane Doe", "01811111111", "Wife"))
                .allergies(List.of("penicillin")).id("PAT-00000");
    }

    private static PatientService newPatientService(Path dir) {
        return new PatientService(new FilePatientRepository(dir.resolve("p.txt")),
                new SequentialIdGenerator("PAT", 0), new EventBus(), noopAudit(), new SessionContext());
    }

    private static AuditLogger noopAudit() {
        return new AuditLogger() {
            @Override public void log(AuditEntry entry) { }
            @Override public void close() { }
        };
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); } catch (Throwable t) {
            if (type.isInstance(t)) return;
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " but nothing was thrown");
    }

    private static void run(String name, ThrowingRunnable test) {
        try { test.run(); passed++; System.out.println("PASS  " + name); }
        catch (Throwable t) { failed++; System.out.println("FAIL  " + name + " -> " + t.getMessage()); }
    }

    @FunctionalInterface private interface ThrowingRunnable { void run() throws Exception; }
}

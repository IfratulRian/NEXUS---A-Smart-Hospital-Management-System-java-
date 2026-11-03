# NEXUS - Continuation Prompt (paste this, together with the project zip/files, into a new chat)

I am building **NEXUS - Smart Hospital Management System**, a Java console OOP university project
(original master spec: the "NEXUS Master Development Prompt" I already have; its rules still apply:
deep package structure, no god classes/methods, tiny `main()`, quality over line count, explain which
OOP concept each decision demonstrates, minimal comments).

## What is ALREADY DONE (do not rewrite - extend it)
Package root `nexus`, entry `Main -> ApplicationLauncher.start()`.
- application: launcher, bootstrap (`ApplicationBootstrap` composition root, `ApplicationContext` record), config, `PatientService`, `PatientSortOption`, `DoctorService`, `DoctorRegistration`
- domain: `Person`(abstract) -> `Patient`(Builder, status state machine, `EmergencyContact` record), `Employee`(abstract) -> `Doctor`, `Nurse`; enums `Gender`, `BloodGroup`, `DepartmentType`, `Shift`, `PatientStatus`; `Identifiable<T>`; `PatientRegisteredEvent`
- infrastructure: generic `Repository<T,ID>`, `PatientRepository`, `DoctorRepository`, `UserRepository`, abstract `FileRepository<T>` (tab-separated, template method) + File* implementations; `AuditLogger`/`AsyncFileAuditLogger`/`AuditEntry`/`AuditAction`; `Notifier`/`ConsoleNotifier`/`NotificationService`
- security: `Role`, `Permission`, `User`, `Authenticatable`, `PasswordHasher`/`Sha256PasswordHasher`, `SessionContext`, `AuthenticationService`, `AuthorizationService`
- patterns: `EventBus`/`EventListener<E>`/`DomainEvent` (observer), `UserFactory`
- presentation.console: `ConsoleInput` (safe input), `ConsoleOutput`, `ConsoleFormatter`, `TableFormatter`, `Menu` (template, permission-filtered), `MainMenu`, `PatientMenu`, `DoctorMenu`, `LoginScreen`, `ConsoleApplication`
- utility: `Validator<T>`, `Validators`, `IdGenerator`/`SequentialIdGenerator`, `DateParser`
- exception: `NexusException` + Validation, Authentication, Authorization, Repository, DuplicateEntity, EntityNotFound, InvalidPatient, InvalidDoctor
- tests: dependency-free `test/nexus/NexusTests.java` (8 passing tests); `./build.sh run|test`
Approx. 2,000 lines of Java (79 files) so far. Default login admin/admin123. Everything compiles and runs.

## REMAINING WORK (implement phase by phase, compile-check each phase, keep the same architecture & style)
Before each phase state the files you will add and which OOP concept/pattern each demonstrates.

1. **Hospital structure (Phase 6)**: `Department` (aggregates doctors/nurses), `Ward`, `Room`, `Bed` (composition), `Hospital` aggregate; `DepartmentRepository`; bed allocation on admission; admission/discharge history on `Patient` (or separate `Admission` entity).
2. **Nurse & other staff**: `Pharmacist`, `Receptionist`, `Administrator` extending `Employee`; `NurseRepository`; staff-to-User linking; admin screen to create users (use `UserFactory`, `USER_MANAGE` permission); `NurseMenu`, `UserMenu`.
3. **Appointments (Phase 7)**: `Appointment` with State pattern (`SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW` as state classes/enum behaviour), priority, doctor availability (`Schedulable`), conflict detection, reschedule/cancel, history; `AppointmentService`, `AppointmentRepository`, events (`AppointmentCreatedEvent`, `AppointmentCancelledEvent`), `AppointmentMenu`; hook `NotificationService` for these events (add `EmailNotifier`, `SmsNotifier`, `NotifierFactory`).
4. **Medical records & prescriptions (Phases 8-9)**: `MedicalRecord` (Builder; composition of `Diagnosis`, `Treatment`, `TestResult`, `Prescription`, doctor notes, visits), `Prescription` with items, `MedicalRecordService`, repositories, `MedicalRecordMenu`.
5. **Pharmacy (Phase 10)**: `Medicine` hierarchy (`Tablet, Capsule, Syrup, Injection, Ointment`, only with real behaviour e.g. dosage-unit/price calc), batch, expiry, supplier, min stock; `PharmacyService` (add/update/remove/search/dispense by prescription), `MedicineLowStockEvent`, background low-stock/expiry checker (`ScheduledExecutorService`), `PharmacyMenu`.
6. **Emergency (Phase 11)**: `EmergencyCase` (State pattern for treatment status), `TriageLevel` (CRITICAL, URGENT, MODERATE, LOW), `Ambulance`, `PriorityQueue`-based `TriageQueue` using Comparator (severity then arrival time), `EmergencyService`, `EmergencyCreatedEvent`, `EmergencyMenu`.
7. **Billing (Phase 12)**: `Billable` interface; `ConsultationCharge, MedicineCharge, RoomCharge, LaboratoryCharge, EmergencyCharge, SurgeryCharge`; `Invoice` (Builder, aggregates billables), `Payment` hierarchy + `PaymentStrategy` (`Cash, Card, MobileBanking, Insurance`) + `PaymentFactory`; `BillingService`, `PaymentCompletedEvent`, `InvoiceRepository`, `BillingMenu`.
8. **Insurance (Phase 13)**: `InsuranceProvider`, `InsurancePolicy` (coverage %, max coverage, eligible services), `Claim` with State pattern (`SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, PAID`), `InsuranceService`, `InsuranceMenu`.
9. **Reports (Phase 19)**: `Report` interface + implementations (patients totals/admitted, doctors by department, appointments today/completed/cancelled, emergency cases, medicine stock/low stock, revenue, outstanding payments, insurance claims, doctor workload) using Streams/Collectors; `ReportMenu`; optional async report generation (`CompletableFuture`).
10. **Dashboards**: role-based dashboard screen after login showing key counts for that role.
11. **Search/filter/sort extras (Phase 20)**: strategies for doctors/medicines/appointments/emergency (Comparator/Comparable); filter appointments by status/date.
12. **Persistence completeness**: `data/` sub-folders from the spec (patients, doctors, appointments, medicines, invoices, users, departments, emergency, logs); ensure IDs for DOC/NUR/APP/MED/INV/EMG/PAY via `SequentialIdGenerator`; keep models free of file code.
13. **Role dashboards & authorization**: add the remaining `Permission` checks to every new menu item through `Menu.add(label, permission, action)`; no role checks scattered in services.
14. **Testing (Phase 23)**: extend `NexusTests` (or migrate to JUnit 5) for appointment conflicts, doctor availability, emergency priority ordering, medicine stock/dispensing, billing totals, payment strategies, insurance coverage, claim state transitions, authentication.
15. **Refactoring & polish (Phase 24)**: remove duplication between menus (shared helpers), update README OOP concept table, ensure no class > ~250 lines, no method > ~40 lines, `main()` unchanged.

## Rules for the continuation
- Java 17+, no external libraries, console only, keep JavaFX-readiness (no UI code in domain/application).
- Give complete compilable files grouped by phase; after each phase list: new files, concepts demonstrated, and how to test it.
- Do not rewrite finished files unless a change is required; when you must, show the full updated file.
- Work in chunks small enough to finish; if you are running out of space, stop at a clean phase boundary and write an updated "REMAINING" list.

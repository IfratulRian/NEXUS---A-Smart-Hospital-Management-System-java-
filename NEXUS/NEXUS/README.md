# NEXUS - Smart Hospital Management System

A robust, enterprise-grade, console-based Java 17+ Object-Oriented Hospital Management System designed with clean domain-driven architecture and strict separation of concerns.

## Quick Start
```bash
# Compile and run application (default login: admin / admin123)
./build.sh run

# Compile and run test suite
./build.sh test
```

**Windows PowerShell:**
```powershell
New-Item -ItemType Directory -Force -Path out
$sources = Get-ChildItem -Recurse -Filter *.java -Path src | ForEach-Object { $_.FullName }
javac -d out $sources
java -cp out Main
```

Data is created automatically in `data/` on initial startup (patients, doctors, nurses, departments, appointments, medical records, medicines, emergency, invoices, insurance, users, and audit logs).

---

## Architectural Layers (Inward-Pointing Dependencies)
```text
Main -> ApplicationLauncher -> ApplicationBootstrap (Composition Root) -> ConsoleApplication
 presentation  --->  application (domain services)  --->  domain  <---  infrastructure (file repos, logging, notifications)
 [Shared Support: security, patterns, exception, utility]
```

- **Domain Layer**: Clean business models, state machines, value objects, and domain events. Completely free of UI or file I/O dependencies (JavaFX-ready).
- **Application Layer**: Use-case orchestration services (`PatientService`, `DoctorService`, `HospitalService`, `AppointmentService`, `MedicalRecordService`, `PharmacyService`, `EmergencyService`, `BillingService`, `InsuranceService`, `ReportService`, `DashboardService`, `UserService`).
- **Infrastructure Layer**: Template-method based file repositories with atomic caching and serialization, asynchronous file audit logging, and multi-channel notifications (Console, Email, SMS).
- **Presentation Layer**: Safe console input validation, formatted tables, permission-filtered template menus, and post-login role dashboards.
- **Security & Authorization**: Salted SHA-256 password hashing, user factory, and role-based permissions (`Role` -> `Permission` set).

---

## Comprehensive OOP Concept Map

| Code Symbol / Pattern | Object-Oriented Concept / Design Pattern | Rationale & Demonstration |
|---|---|---|
| `Person` abstract; `Patient`, `Employee` extend it | **Abstraction & Class Inheritance** | `Person` defines core human attributes; `Employee` introduces departmental employment. |
| `Doctor`, `Nurse`, `Pharmacist`, `Receptionist`, `Administrator` | **Specialization & Hierarchy** | Distinct professional roles with role-specific responsibilities and credentials. |
| `Person.getRoleLabel()` | **Runtime Polymorphism (Method Overriding)** | Subclasses override polymorphic role labels rendered dynamically in reports/tables. |
| Private fields + validated setters in `Person` & entities | **Encapsulation & Information Hiding** | State cannot be corrupted; validated setters protect business invariants. |
| `Doctor` constructors (default fee vs explicit fee) | **Method / Constructor Overloading** | Provides convenient instantiation defaults while allowing custom fee overrides. |
| `Identifiable<T>`, `Authenticatable`, `Repository<T,ID>`, `Billable`, `Schedulable` | **Interface Segregation & Dependency Inversion** | Services depend on minimal abstract interfaces rather than concrete classes. |
| `FileRepository<T>` -> concrete repos | **Template Method Pattern & DIP** | Base class controls file loading, memory caching, and disk flush; subclasses define TSV mapping. |
| `Patient.Builder`, `MedicalRecord.Builder`, `Invoice.Builder` | **Builder Pattern** | Constructs complex domain aggregates with many required/optional fields and validation. |
| `UserFactory`, `PaymentFactory`, `NotifierFactory` | **Factory Pattern** | Encapsulates instantiation and configuration of strategies, users, and notifiers. |
| `PatientSortOption`, `DoctorSortOption`, `MedicineSortOption` | **Strategy Pattern (Enum-based)** | Encapsulates `Comparator` algorithms as first-class selectable strategy constants. |
| `PaymentStrategy` (`CashPayment`, `CardPayment`, `MobileBankingPayment`, `InsurancePayment`) | **Strategy Pattern & Behavioral Polymorphism** | Executes payment processing strategies interchangeably without branching logic. |
| `EventBus`, `DomainEvent`, `EventListener<E>` | **Observer Pattern** | Decouples event publishers (`PatientService`, `AppointmentService`, `EmergencyService`) from listeners. |
| `AppointmentStatus`, `EmergencyStatus`, `ClaimStatus`, `PatientStatus` | **State Pattern (State Machines)** | Enforces strict lifecycle state transitions (`allowedNext()`) and terminal state handling. |
| `Room` has `Bed`s; `Ward` has `Room`s; `Department` has `Ward`s | **Object Composition** | Lifecycle-bound ownership hierarchy (rooms, beds, and wards live with their parent). |
| `Department` references `Doctor` & `Nurse` IDs | **Object Aggregation** | Staff members exist independently in their own repositories and can be assigned/reassigned. |
| `Hospital` aggregate root | **Aggregate Root & Information Expert** | Manages hospital-wide capacity, departments, and inpatient bed occupancy rates. |
| `TriageQueue` with `PriorityQueue` | **Data Structures & Custom Comparators** | Orders emergency trauma cases by clinical acuity rank (1 = Critical), tie-breaking by arrival time. |
| `Medicine` abstract base -> `Tablet`, `Capsule`, `Syrup`, `Injection`, `Ointment` | **Polymorphic Hierarchy with Real Behavior** | Subclasses override `calculatePrice()` with strip/box discounts or sterile handling fees. |
| `Report` interface + `CompletableFuture` async generation | **Multithreading & Functional Composition** | Demonstrates non-blocking analytical report generation with Java Streams and Collectors. |
| `AsyncFileAuditLogger` (Single-Thread Executor) | **Concurrency / Asynchronous I/O** | Offloads file audit logging to background thread to avoid blocking core operations. |
| `ScheduledExecutorService` in `PharmacyService` | **Background Task Execution** | Runs automated periodic checks for low-stock inventory alerts and drug batch expiration. |
| `Menu` template -> 12 domain submenus | **Template Method Pattern** | Menu loop, rendering, error trapping, and permission filtering live once in `Menu`. |
| `Role` -> `Permission` + `AuthorizationService` | **Role-Based Access Control (RBAC)** | Submenus declaratively specify required permissions; services never hardcode roles. |
| `Validator<T>.and(...)`, `Validators` | **Functional Interfaces & Combinator Pattern** | Validates email, phone, name, and dates composably without external libraries. |
| Custom exception hierarchy (`NexusException`, `ConflictException`, etc.) | **Exception Hierarchy** | Distinguishes validation, repository, authorization, conflict, and domain errors. |

---

## Completed Modules & Submenus
1. **Patient Management (`PatientMenu`)**: Registration (Builder), search, sort strategies, status transitions.
2. **Doctor Staffing (`DoctorMenu`)**: Registration, sort by fee/experience, search by specialization.
3. **Nurse Staffing (`NurseMenu`)**: Registration, department/shift filtering, ward reassignments.
4. **Hospital & Bed Management (`HospitalMenu`)**: Ward/room/bed allocation, inpatient admission & discharge.
5. **Appointment Scheduling (`AppointmentMenu`)**: `Schedulable` conflict detection, reschedule, cancel, state workflow.
6. **Medical Records (`MedicalRecordMenu`)**: Diagnoses, treatments, lab test results, clinical notes, prescriptions.
7. **Pharmacy & Inventory (`PharmacyMenu`)**: Polymorphic drug hierarchy, stock control, prescription dispensing, low-stock/expiry alerts.
8. **Emergency Trauma (`EmergencyMenu`)**: PriorityQueue-based triage, trauma state workflow, ambulance fleet dispatch.
9. **Billing & Invoices (`BillingMenu`)**: Polymorphic `Billable` items, Builder invoice, Strategy-based multi-channel payments.
10. **Health Insurance (`InsuranceMenu`)**: Policy limits, deductible calculation, claim adjudication state machine, settlement.
11. **Analytical Reports (`ReportMenu`)**: Stream-based analytics, status distributions, async parallel report generation.
12. **User Account Security (`UserMenu`)**: Account creation with `UserFactory`, password hashing, staff-to-user linking, enable/disable.
13. **Role Dashboards**: Customized KPI summary cards presented after login based on the authenticated role.

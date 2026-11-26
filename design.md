# Design

## Object Model

NEXUS separates core hospital concepts into domain aggregates and supporting value objects.

### People

`Person` is the abstract base for people.
`Employee` extends the people model for staff.
`Patient`, `Doctor`, `Nurse`, and other staff retain domain-specific behavior and validation.
Builders are used where construction has many fields or needs invariant checking.

### Hospital

`Hospital` is the aggregate for departments and physical resources.
Departments aggregate doctors and nurses.
Wards contain rooms, rooms contain beds, and admissions allocate and release beds.

### Clinical Workflow

`Appointment` coordinates a patient and doctor at a scheduled time.
`MedicalRecord` owns or aggregates the clinical record, diagnoses, treatments, test results,
doctor notes, and prescriptions.
`Prescription` owns prescription items and tracks dispensing status.

### Pharmacy

Medicine behavior varies by medicine subtype.
Stock changes use explicit stock reduction rules and low-stock checks.
Dispensing is coordinated through the pharmacy and medical-record services.

### Emergency

`TriageQueue` uses severity and arrival order.
Emergency status transitions are explicit and prevent invalid jumps.

### Billing

`Invoice` aggregates `Billable` charges and calculates the balance.
Payment behavior is polymorphic through payment types and strategies.

### Insurance

`InsurancePolicy` calculates coverage and remaining limits.
`Claim` transitions through submitted, review, approval/rejection, and paid states.

## Key Patterns

- Builder: complex patient, medical record, or invoice construction.
- Repository: persistence abstraction for domain collections.
- Template method: shared file-repository behavior.
- Factory: user and notifier creation.
- Observer: domain event bus and notifications.
- State: patient, appointment, emergency, and insurance lifecycles.
- Strategy: sorting, payment, and report behavior.
- Composite/aggregate: hospital, medical record, and invoice ownership.
- Composition root: `ApplicationBootstrap`.
- Dependency injection: constructor-based service wiring.

## Menu and Dashboard Design

`Menu` provides permission-filtered console choices.
Feature menus call application services and never access repositories directly.
`ConsoleApplication` performs login, shows a role dashboard, runs the main menu, and logs out.

## Data and Serialization

File repositories own serialization details.
Each model has an identity and does not embed file-format code.
TSV/text values are parsed or written by infrastructure implementations.

## Error Handling

`NexusException` is the base project exception.
Specializations cover validation, authentication, authorization, persistence, duplicate,
entity-not-found, and domain-specific invalid operations.
Console startup and login/reporting paths translate exceptions into user-visible messages.

## Future UI Readiness

Keep JavaFX or web UI code outside domain and application layers.
Services should expose complete use-case operations that a future UI can call without console types.

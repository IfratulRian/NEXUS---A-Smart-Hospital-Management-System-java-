# Product Requirements Document

## Product

NEXUS is a smart hospital management system for operating a hospital through a console interface.
It supports clinical, administrative, pharmacy, emergency, billing, insurance, reporting, and
role-based dashboard workflows.

## Goals

- Provide a single local system for core hospital operations.
- Keep business rules explicit in domain classes and application services.
- Restrict actions using roles and permissions.
- Preserve operational data across restarts using text/file repositories.
- Keep the architecture suitable for a future graphical interface.
- Remain easy to compile and test without external dependencies.

## Primary Users

- Administrators
- Reception or front-desk staff
- Doctors
- Nurses
- Pharmacists
- Billing and insurance staff
- Emergency staff
- Report consumers

## Functional Requirements

### Patient and Staff

- Register, search, update, and sort patients.
- Manage doctor, nurse, and hospital staff records.
- Show patient status and admission history.
- Create and manage user accounts with role and status rules.

### Hospital Operations

- Model departments, wards, rooms, beds, and hospital occupancy.
- Allocate and release beds during admission workflows.
- Record admission and discharge history.

### Appointments

- Schedule appointments for a patient and doctor.
- Detect doctor or patient conflicts.
- Reschedule, confirm, start, complete, cancel, and mark no-shows.
- Filter appointments by date and related doctor or patient.

### Clinical Records

- Create and retrieve medical records.
- Attach diagnoses, treatments, test results, notes, and prescriptions.
- Create and track prescription items and dispensing status.

### Pharmacy

- Add, update, remove, and search medicine inventory.
- Reduce stock safely when dispensing.
- Reject dispensing that exceeds stock.
- Surface low-stock and expired medicine conditions.

### Emergency

- Capture emergency cases and triage levels.
- Queue cases by severity and arrival time.
- Transition cases through emergency treatment states.

### Billing and Insurance

- Create invoices from consultation, medicine, room, laboratory, emergency, and surgery charges.
- Calculate subtotals, discounts, tax, and balances.
- Support polymorphic payment strategies.
- Create and transition insurance claims and respect policy coverage.

### Reporting and Dashboards

- Generate patient, staff, appointment, emergency, inventory, revenue, billing, and claim reports.
- Show role-based metrics after login.

## Non-Functional Requirements

- Java 17+ compatibility.
- Small `Main` entry point and clear composition root.
- No UI dependencies inside domain or application code.
- Data remains accessible in `data/`.
- Audit meaningful authentication and business operations.
- Menu actions must have authorization checks.
- Unit/integration tests must cover core state and calculation rules.

## Acceptance Criteria

- A clean build succeeds with `build.sh`.
- The test runner reports no failures.
- The application starts and accepts the demo administrator login.
- Invalid patient, appointment, stock, invoice, insurance, and authentication operations are rejected.
- Restarting the application retains data and does not duplicate demo records.
- Unauthorized menu operations are not available to the current role.

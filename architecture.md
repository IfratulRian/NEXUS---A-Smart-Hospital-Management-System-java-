# Architecture

## Entry and Runtime Flow

```text
Main
  -> ApplicationLauncher
  -> ApplicationBootstrap
  -> ApplicationContext
  -> ConsoleApplication
  -> role-based menu and dashboard
```

`Main` delegates immediately to `ApplicationLauncher`.
`ApplicationBootstrap` is the composition root and wires repositories, services, security,
event infrastructure, ID generators, and audit logging.
`ApplicationContext` exposes the resulting application object graph.

## Package Boundaries

### Domain

Contains entities, value objects, enums, domain events, and business state machines.
Domain code must not know about console input, Java Swing/JavaFX, or file paths.

Examples include `Patient`, `Doctor`, `Nurse`, `Appointment`, `MedicalRecord`, `Medicine`,
`EmergencyCase`, `Invoice`, `Payment`, `InsurancePolicy`, and `Hospital`.

### Application

Coordinates domain behavior and infrastructure operations.
Application services receive repositories and utility dependencies through constructors.
They enforce use-case permissions and audit records where required.

### Infrastructure

Contains file repositories, repository interfaces, audit logging, notification delivery,
and concrete persistence implementations.
The file repository hierarchy owns TSV/text parsing and serialization.

### Presentation

Contains console menus, input/output helpers, formatters, screens, and dashboards.
Presentation code translates console interaction into application-service calls.

### Security

Contains users, roles, permissions, session state, password hashing, authentication,
and authorization services.

### Patterns and Utilities

Contains reusable factory, observer, ID generation, validation, date, and sorting utilities.

## Persistence

Data is stored under `data/` in text or TSV files.
The data layout includes patients, doctors, nurses, departments/admissions, appointments,
medical records, medicines, emergency cases, invoices, insurance, users, and audit logs.

Repository implementations isolate file access from domain and application code.

## Composition and Lifecycle

- `ApplicationConfig.defaults()` supplies the hospital name and `data` path.
- `ApplicationBootstrap.initialize(...)` creates and seeds repositories.
- ID generators are seeded from existing persisted IDs.
- `EventBus` delivers domain events to notification and observer implementations.
- `ApplicationContext.shutdown()` stops background work and closes audit logging.
- `ConsoleApplication` owns the interactive run loop and role dashboard display.

## State and Strategy Patterns

Key stateful domains include patient status, appointments, emergency cases, and insurance claims.
Payment and charge behavior is modeled through polymorphic types and strategy abstractions.
Sorting and filtering use explicit option or comparator classes.

## Dependency Direction

```text
Presentation -> Application -> Domain
Infrastructure -> Domain
Application -> Infrastructure interfaces
```

Concrete infrastructure dependencies are created only at the composition root.

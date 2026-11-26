# Project Memory

## Current Project

NEXUS is a console-based Smart Hospital Management System implemented in Java.
The repository is a Java 17+ project with no external runtime dependencies.

## Repository Layout

- `NEXUS/NEXUS/src/` - Java source code.
- `NEXUS/NEXUS/src/nexus/application/` - use-case and composition logic.
- `NEXUS/NEXUS/src/nexus/domain/` - hospital business entities and rules.
- `NEXUS/NEXUS/src/nexus/infrastructure/` - persistence, logging, and notifications.
- `NEXUS/NEXUS/src/nexus/presentation/console/` - console UI.
- `NEXUS/NEXUS/src/nexus/security/` - authentication, authorization, users, and roles.
- `NEXUS/NEXUS/src/nexus/patterns/` - factories and observer implementations.
- `NEXUS/NEXUS/test/nexus/NexusTests.java` - dependency-free test runner.
- `NEXUS/NEXUS/data/` - text file persistence.
- `NEXUS/NEXUS/build.sh` - compile and run/test script.

## Implemented Baseline

The code has a complete application composition root and console entry flow:
`Main -> ApplicationLauncher -> ApplicationBootstrap -> ConsoleApplication`.

Implemented areas include:

- Patient, doctor, nurse, hospital, and staff services.
- Department, ward, room, bed, admission, and discharge models.
- Appointment scheduling, lifecycle, conflicts, and cancellation.
- Medical records and prescriptions.
- Medicine catalogs, stock control, scheduled low-stock monitoring, and dispensing.
- Emergency triage queue and emergency lifecycle.
- Invoices, billable charges, payments, and insurance claims/policies.
- Reports and role-aware dashboards.
- User management, authentication, permissions, sessions, audit logging, and notifications.
- File repositories and demo data seeding.

## Established Technical Decisions

- Java 17 or newer.
- Console-only presentation.
- No external libraries.
- File-based TSV/text persistence under `data/`.
- Domain classes must not perform file I/O.
- Application services coordinate repositories, domain behavior, events, logging, and authorization.
- `ApplicationBootstrap` is the composition root.
- `ApplicationContext` exposes the wired services.
- `Main` remains a tiny entry point.
- Authorization is expressed through `Permission` values and menu registration.

## Demo Environment

Default administrator credentials are `admin / admin123`.
Demo seeding fills empty catalogs and preserves existing data.

## Current Risks to Remember

- The project contains a large and unevenly authored README; generated class tables may become stale.
- `REMAINING_PROMPT.md` describes an earlier development plan and should not be treated as evidence
  that a feature is absent.
- There are currently 197 Java source files and one source test runner file.
- Audit and data files are runtime/generated state and should be reviewed before commits.

## Session Notes

- Update this file when architecture or major feature ownership changes.
- Record only verified decisions; do not copy planned work as completed state.

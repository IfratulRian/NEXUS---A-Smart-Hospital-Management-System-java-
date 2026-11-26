# Development Rules

## Toolchain

- Use Java 17 or newer.
- Do not add external libraries without an explicit requirement.
- Use the existing `build.sh` workflow.
- Preserve `Main.main()` as the application entry point.
- Keep console-only presentation separate from domain and application code.

## Source Organization

- Keep packages under `nexus`.
- Place domain types in `nexus.domain`.
- Place use-case coordination in `nexus.application`.
- Place file I/O and external integrations in `nexus.infrastructure`.
- Place console interaction in `nexus.presentation.console`.
- Place authentication and authorization in `nexus.security`.

## Coding Constraints

- Prefer small, focused classes and methods.
- Target no class larger than roughly 250 lines.
- Target no method longer than roughly 40 lines.
- Use constructors or builders for valid aggregate creation.
- Validate identifiers, names, dates, amounts, and external inputs at boundaries.
- Return read-only views for mutable collections.
- Do not expose mutable repository data through domain getters.
- Keep `Main` small and delegate startup and shutdown cleanly.
- Avoid god classes and unrelated responsibilities.

## Persistence

- Domain classes must not read or write files.
- Use repository interfaces and implementations for data access.
- Preserve existing data files across normal application starts.
- Seed demo data only when the relevant collection is empty.
- Use `SequentialIdGenerator` for generated domain identifiers.
- Update repositories and tests together when persistence changes.

## Authorization

- Use `Role` and `Permission` for access decisions.
- Register menu actions with the existing permission mechanism.
- Do not scatter role checks throughout application services.
- Fail closed for unknown or unauthorized actions.

## Events and Notifications

- Publish domain events for meaningful state transitions.
- Keep notification delivery behind the notifier/notification abstractions.
- Do not make domain entities depend on console output.

## Testing

- Extend `NexusTests.java` unless the project intentionally migrates to JUnit 5.
- Test state transitions, validation, persistence, calculations, conflict detection,
  authorization, and stock or coverage boundaries.
- Use temporary directories for repository tests.
- Keep tests dependency-free.
- Run the smallest relevant test first, then the full test runner.

## Git and Change Hygiene

- Do not commit runtime `out/` output or local IDE state.
- Review `data/logs/audit.log` before commits because it is generated state.
- Do not rewrite working or unrelated files.
- Make surgical changes and verify the exact requirement.
- Update directly related README or project memory documentation.

## Documentation

- Keep `tasks.md`, `memory.md`, `prd.md`, `architecture.md`, `rules.md`, and `design.md`
  aligned with verified implementation.
- Do not mark planned features as complete.
- Record verified commands and test results.

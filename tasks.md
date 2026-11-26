# Tasks

## Current Status

The main implementation is present and builds as a Java 17+ console application.
The primary source tree is `NEXUS/NEXUS/src`, with a dependency-free test runner at
`NEXUS/NEXUS/test/nexus/NexusTests.java`.

## Immediate Validation

- [ ] From `NEXUS/NEXUS`, run `./build.sh test` in a compatible shell.
- [ ] From `NEXUS/NEXUS`, run `./build.sh run` and verify login with `admin / admin123`.
- [ ] Verify one workflow in each menu: patient, doctor, hospital, appointment, record,
      pharmacy, emergency, billing, insurance, report, and user management.
- [ ] Run from a clean data directory and verify demo seeding happens once.
- [ ] Verify existing data files are preserved and are not overwritten by seeding.

## Open Work

- [ ] Audit the 197 source files against `REMAINING_PROMPT.md`; mark completed prompt phases as done.
- [ ] Review every console menu action for the required `Permission` check.
- [ ] Add or strengthen tests for each service and repository boundary.
- [ ] Verify all ID prefixes use `SequentialIdGenerator`: PAT, DOC, NUR, APP, MED, INV, PAY,
      EMG, ADM, REC, PRX, and CLM.
- [ ] Check file persistence round trips for all major domain aggregates.
- [ ] Keep domain and application classes independent from console and file-format concerns.
- [ ] Ensure new classes stay near the documented size limits.
- [ ] Update `README.md` architecture and OOP concept tables after every structural change.

## Suggested Implementation Order

1. Complete test and gap analysis.
2. Fix correctness or persistence defects found by the test pass.
3. Add authorization coverage to any menu action that lacks a permission.
4. Improve and de-duplicate console menu code.
5. Add JUnit 5 migration only if the existing dependency-free runner becomes insufficient.
6. Perform final README, data, and build-script verification.

## Definition of Done

- Source and tests compile on Java 17+.
- Existing tests pass.
- No unrelated file or data changes are introduced.
- Persistence remains file-based and data is not seeded repeatedly.
- Menu actions enforce authorization through the existing menu and session abstractions.
- Changes include relevant tests and documentation.

---
description: Task list for feature implementation
---

# Tasks: 005-add-solid-benchmarks-docs

**Input**: Design documents from `/specs/005-add-solid-benchmarks-docs/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md
**Tests**: For this repository, tests are **mandatory** for new production code (JUnit 5, under `src/test/java`).
**Organization**: Tasks are grouped by user story (US1/US2/US3) for independent implementation and testing.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare shared helpers and test scaffolding for Etapa 5 outputs

- [X] T001 [P] Add a new test-only helper `src/test/java/com/solidanalysis/scoring/HumanReadableResultsTestUtils.java` to load `scoring/<Class>.json` and `scoring/project_summary.json` and to read generated `results/<Class>.txt` and `results/project_summary.txt` as strings for assertions
- [X] T002 Create/verify Maven test setup is compatible with additional JSON parsing in Etapa 5 tests (`src/test/java/com/solidanalysis/scoring/`)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core formatter mapping used by both US1 and US3 (required before user stories)

⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T003 Create production class `src/main/java/com/solidanalysis/scoring/HumanReadableResultsWriter.java` with public method stubs for `writeClassReports(...)` and `writeProjectSummary(...)`, risk-to-symbol mapping (`ALTO -> [!]`, `MEDIO -> [~]`, `BAIXO -> [ok]`), letter-to-principle-name mapping (`S,O,L,I,D`), private formatting helpers for separators and headers, and Javadoc for all public APIs introduced/changed
- [X] T004 [P] Create unit test `src/test/java/com/solidanalysis/scoring/HumanReadableResultsWriterTest.java` that validates symbol mapping and principle header line prefix mapping (e.g., `O — Open/Closed:`)

---

## Phase 3: User Story 1 - Generate per-class human-readable reports (Priority: P1) 🎯 MVP

**Goal**: Generate `results/<Classe>.txt` for each class scored, with correct header and principle sections/symbols.

**Independent Test**: Run `SolidAnalysisCli.run --score` on the existing scoring fixture output and assert:
- `results/<Classe>.txt` exists for every scored class
- The header and all principle lines exist and use correct symbols.

### Tests for User Story 1 (required — JUnit 5 under `src/test/java`) ⚠️

> NOTE: Write these tests FIRST, ensure they FAIL before implementation

- [X] T005 [US1] [P] Add integration test `src/test/java/com/solidanalysis/scoring/HumanReadableResultsWriterClassReportsIntegrationTest.java` that creates a temp output dir, uses `ScoringTestFixtures.copyOutputFixture(...)` to materialize Stage 4 scoring into `scoring/`, executes scoring via `SolidAnalysisCli.run(new String[] {"--score", ...})`, asserts `results/<Class>.txt` exists for each `<Class>.json` under `scoring/`, and asserts each `results/<Class>.txt` contains `Legenda de símbolos:`
- [X] T006 [US1] Validate formatting for one known class (e.g., `ContaCorrente`): in `results/ContaCorrente.txt`, assert presence of `CLASSE: ContaCorrente`, `PROJETO:`, `SCORE GERAL:`, `Legenda de símbolos:` and all principle headers `O — Open/Closed:`, `S — Single Responsibility:`, `L — Liskov Substitution:`, `I — Interface Segregation:`, `D — Dependency Inversion:`; also assert:
  - For each principle letter where `scoring/ContaCorrente.json` has at least one indicator, the txt contains at least one indicator `detail` line whose text matches an indicator `detail` from the JSON and is prefixed with the correct symbol for that principle risk

### Implementation for User Story 1

- [X] T007 [US1] Implement `HumanReadableResultsWriter.writeClassReports(...)` in `src/main/java/com/solidanalysis/scoring/HumanReadableResultsWriter.java` to generate the exact header structure with `SCORE GERAL` and `ESTRATÉGIA` (including factor text for relaxed fixed-threshold runs when present), principle sections in the order `O, S, L, I, D`, indicator `detail` lines prefixed with the symbol of the principle’s risk level, and the mandatory `Legenda de símbolos:` mapping section (keep/extend Javadoc as needed for any new public helpers)
- [X] T008 [US1] Update `src/main/java/com/solidanalysis/scoring/ScoringRunner.java` to create `projectOutputDir/results/` (next to `projectOutputDir/scoring/`), call `HumanReadableResultsWriter.writeClassReports(...)` after writing `scoring/<Class>.json` files, and do NOT generate `results/project_summary.txt` yet (reserved for US3) — *superseded by T016: both class reports and project summary are written in the same runner step*

---

## Phase 4: User Story 2 - Add SOLID benchmark projects (Priority: P2)

**Goal**: Add the required `benchmarks/` directory structure with benchmark Java sources:
- `benchmarks/bad-project/` (supermarket with documented SOLID violations)
- `benchmarks/good-project/` (supermarket following SOLID)
- subpastas de referência por princípio (`*_bad`/`*_good`)

**Independent Test**: Manual verification by checking:
- correct directories exist
- `.java` files exist inside each package/subdir
- `bad-project/` contains `SOLID-VIOLATION:` markers above problematic code sections

- [X] T009 Create directories and scaffolding for benchmarks: `benchmarks/bad-project/`, `benchmarks/good-project/`, and principle pairs `benchmarks/*_bad`, `benchmarks/*_good`
- [X] T010 Populate principle reference folders (`benchmarks/*_bad`, `benchmarks/*_good`) with Java source fixtures preserving expected package/file structure, with no network dependency during tests/CI
- [X] T011 Populate `benchmarks/bad-project/` supermarket code (approx. ~15 classes) with the specified domain entities (Product, Category, Customer, Cart, CartItem, Order, Payment, Receipt, Inventory, Discount, Cashier, Register), a centralized `SupermarketManager` (GOD CLASS) with explicit SOLID violations, and comment markers `SOLID-VIOLATION: <principle>` immediately above each problematic snippet
- [X] T012 Populate `benchmarks/good-project/` supermarket code (approx. ~25 classes) with the same domain entities as bad-project, organized by responsibilities into `domain/`, `repository/`, `usecase/`, `service/` packages, and no `SOLID-VIOLATION:` markers

---

## Phase 5: User Story 3 - Generate project_summary.txt with ranking (Priority: P3)

**Goal**: Generate `results/project_summary.txt` with distribution and class ranking order derived from `scoring/project_summary.json`.

**Independent Test**: Run `SolidAnalysisCli.run --score` and assert:
- `results/project_summary.txt` exists
- The ranking block lists classes in the same order as the `ranking` array in `scoring/project_summary.json`.

### Tests for User Story 3 (required — JUnit 5 under `src/test/java`) ⚠️

> NOTE: Write these tests FIRST, ensure they FAIL before implementation

- [X] T013 [US3] Add integration test `src/test/java/com/solidanalysis/scoring/HumanReadableResultsProjectSummaryIntegrationTest.java` that materializes fixture output via `ScoringTestFixtures.copyOutputFixture(...)`, runs `SolidAnalysisCli.run --score` on that directory, reads `scoring/project_summary.json` and `results/project_summary.txt`, and parses the ranking lines to assert ranking order and count match `ranking[]` in the JSON (includes T014 header assertions)
- [X] T014 [US3] Verify presence of header lines in `results/project_summary.txt`: `RESUMO DO PROJETO:`, `ESTRATÉGIA DE CLASSIFICAÇÃO:`, `PRINCÍPIO MAIS VIOLADO:`

### Implementation for User Story 3

- [X] T015 [US3] Implement `HumanReadableResultsWriter.writeProjectSummary(...)` in `src/main/java/com/solidanalysis/scoring/HumanReadableResultsWriter.java` to generate correct header + distribution lines (S/O/L/I/D with ALTO/MEDIO/BAIXO counts) and a ranking block in the exact same order as `ProjectSummary.ranking()` (keep/extend Javadoc as needed for any new public helpers)
- [X] T016 [US3] Update `src/main/java/com/solidanalysis/scoring/ScoringRunner.java` to call `HumanReadableResultsWriter.writeProjectSummary(...)` after building `ProjectSummary summary`

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final wiring and end-to-end verification

- [X] T017 Update smoke test `src/test/java/com/solidanalysis/SolidAnalysisCliTest.java` by extending `scoreModeRunsOnFixtureOutput` to assert `scoring/project_summary.json` exists, `results/project_summary.txt` exists, and at least one `results/<Classe>.txt` exists (preferably all classes, if cheap)
- [X] T018 Run `mvn test` and ensure all tests pass after adding Etapa 5 writer/tests


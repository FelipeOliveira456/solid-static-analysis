# Tasks: ETAPA 4 - Scoring SOLID

**Input**: Design documents from `/specs/004-solid-scoring/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: Mandatory for new production code. Every production task group includes matching JUnit 5 test tasks under `src/test/java`.

**Organization**: Tasks are grouped by user story so each story can be implemented and verified independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (`US1`, `US2`, `US3`)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create shared scaffolding and reusable test helpers for the scoring feature

- [X] T001 [P] Create shared scoring test fixtures in `src/test/java/com/solidanalysis/scoring/ScoringTestFixtures.java` for copying fixture graphs, algorithms, and AST outputs into temp directories
- [X] T002 [P] Add unit tests for shared scoring domain types in `src/test/java/com/solidanalysis/scoring/ScoreModelTest.java`
- [X] T003 [P] Create shared scoring domain types in `src/main/java/com/solidanalysis/scoring/ScoreLevel.java`, `src/main/java/com/solidanalysis/scoring/ScoringStrategy.java`, `src/main/java/com/solidanalysis/scoring/IndicatorTemplate.java`, `src/main/java/com/solidanalysis/scoring/IndicatorResult.java`, `src/main/java/com/solidanalysis/scoring/PrincipleScore.java`, `src/main/java/com/solidanalysis/scoring/ClassScore.java`, and `src/main/java/com/solidanalysis/scoring/ProjectSummary.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core helpers that all scoring stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T004 [P] Add unit tests for project output path resolution and threshold loading in `src/test/java/com/solidanalysis/scoring/ProjectOutputPathResolverTest.java` and `src/test/java/com/solidanalysis/scoring/ThresholdConfigurationTest.java`
- [X] T005 [P] Implement project output path resolution and threshold loading in `src/main/java/com/solidanalysis/scoring/ProjectOutputPathResolver.java` and `src/main/java/com/solidanalysis/scoring/ThresholdConfiguration.java`
- [X] T006 [P] Add unit tests for artifact readers, root AST loading, and scoring-specific DOT parsing in `src/test/java/com/solidanalysis/scoring/ScoringArtifactReaderTest.java`
- [X] T007 [P] Implement artifact readers for root AST JSON, algorithm JSON, and scoring-specific DOT parsing in `src/main/java/com/solidanalysis/scoring/ScoringArtifactReader.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Generate class scores and write `--score` output (Priority: P1) 🎯 MVP

**Goal**: Read a project output directory and produce one scored JSON file per class under `scoring/`

**Independent Test**: Run `java -jar solid-static-analysis.jar --score <ABS_PROJECT_OUTPUT_DIR>` on a valid fixture directory and verify that each class gets a JSON file with S/O/L/I/D scores and an overall score

### Tests for User Story 1 (required — JUnit 5 under `src/test/java`) ⚠️

- [X] T008 [P] [US1] Add CLI smoke tests for `--score` success and invalid absolute-path handling in `src/test/java/com/solidanalysis/SolidAnalysisCliTest.java`
- [X] T009 [P] [US1] Add unit tests for fixed-threshold and z-score classification in `src/test/java/com/solidanalysis/scoring/ClassificationServiceTest.java`
- [X] T010 [P] [US1] Add unit tests for sanitized output path resolution in `src/test/java/com/solidanalysis/scoring/ProjectOutputPathResolverTest.java`

### Implementation for User Story 1

- [X] T011 [P] [US1] Implement fixed-threshold and z-score classification in `src/main/java/com/solidanalysis/scoring/ClassificationService.java`
- [X] T012 [P] [US1] Implement per-class score aggregation and output mapping in `src/main/java/com/solidanalysis/scoring/ScoringRunner.java`
- [X] T013 [US1] Wire `--score` handling into `src/main/java/com/solidanalysis/SolidAnalysisCli.java`
- [X] T014 [US1] Write class score JSON files to `scoring/<Class>.json` in `src/main/java/com/solidanalysis/scoring/ScoringReportWriter.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Produce project summary and ranking (Priority: P2)

**Goal**: Consolidate all class scores into `project_summary.json` with principle distribution and ranking

**Independent Test**: Run the scorer on a valid fixture project and verify `project_summary.json` includes the correct distribution by principle and a deterministic ranking

### Tests for User Story 2 (required — JUnit 5 under `src/test/java`) ⚠️

- [X] T015 [P] [US2] Add unit tests for project summary ranking and distribution in `src/test/java/com/solidanalysis/scoring/ProjectSummaryBuilderTest.java`

### Implementation for User Story 2

- [X] T016 [US2] Implement `project_summary.json` aggregation in `src/main/java/com/solidanalysis/scoring/ProjectSummaryBuilder.java`
- [X] T017 [US2] Integrate summary generation into `src/main/java/com/solidanalysis/scoring/ScoringRunner.java`

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently

---

## Phase 5: User Story 3 - Explain score details and special-case extraction (Priority: P3)

**Goal**: Emit human-readable indicator details and handle the special data sources needed for O, I, and D scoring

**Independent Test**: Run the scorer on fixture data and verify each indicator has a filled `detail`, G6 data is extracted correctly, and abstract classes do not receive concrete-extends indicators

### Tests for User Story 3 (required — JUnit 5 under `src/test/java`) ⚠️

- [X] T018 [P] [US3] Add unit tests for indicator template rendering in `src/test/java/com/solidanalysis/scoring/IndicatorTemplateTest.java`
- [X] T019 [P] [US3] Add unit tests for G6 extraction, abstract-class filtering, and switch-case counting in `src/test/java/com/solidanalysis/scoring/ScoringArtifactReaderTest.java`

### Implementation for User Story 3

- [X] T020 [P] [US3] Implement indicator template interpolation in `src/main/java/com/solidanalysis/scoring/IndicatorTemplate.java`
- [X] T021 [P] [US3] Implement G6 extraction, abstract-class checks, and switch-case parsing in `src/main/java/com/solidanalysis/scoring/ScoringArtifactReader.java`
- [X] T022 [US3] Attach rendered indicator details to class outputs in `src/main/java/com/solidanalysis/scoring/ScoringRunner.java`

**Checkpoint**: At this point, all user stories should be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: CLI orchestration, documentation, and end-to-end validation

- [X] T023 [P] Add `--all` orchestration to `src/main/java/com/solidanalysis/SolidAnalysisCli.java` so the CLI runs scanner, graphs, analyze, and scoring in sequence from an absolute project root
- [X] T024 [P] Add end-to-end smoke test for `--all` in `src/test/java/com/solidanalysis/SolidAnalysisCliTest.java`
- [X] T025 Update `README.md`, `ITERACAO.md`, and `specs/004-solid-scoring/quickstart.md` with `--score` and `--all` usage examples and workflow notes
- [X] T026 Validate the full pipeline with `mvn test` and representative fixture outputs under `src/test/resources/java-fixtures/output/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user stories
- **User Stories (Phase 3+)**: All depend on Foundational completion
  - User stories can proceed in parallel once foundational helpers exist
  - User stories should still be implemented in priority order for MVP value
- **Polish (Final Phase)**: Depends on the user stories that need to be complete for the final CLI experience

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - no dependency on later stories
- **User Story 2 (P2)**: Can start after Foundational - consumes class-score outputs from US1
- **User Story 3 (P3)**: Can start after Foundational - reuses shared readers and output models, and can be completed alongside US2

### Within Each User Story

- Tests should be written first and fail before implementation
- Shared model types and helpers come before services and runners
- Runners should be wired only after their dependencies are available
- Story completion should be validated before moving to the next priority

### Parallel Opportunities

- Setup tasks T001, T002, and T003 can run in parallel after the test scaffolding decision is fixed
- Foundational tasks T004, T005, T006, and T007 can run in parallel in two independent test/implementation pairs
- US1 test tasks T008, T009, and T010 can run in parallel
- US1 implementation tasks T011 and T012 can run in parallel after the foundational tasks
- US2 test task T015 can run independently of US3 tests
- US3 test tasks T018 and T019 can run in parallel
- US3 implementation tasks T020 and T021 can run in parallel after the foundational tasks
- Final-phase tasks T023 and T024 can run in parallel once the CLI shape is settled

---

## Parallel Example: User Story 1

```bash
# Launch the classification and path-resolution tests together:
Task: "Add unit tests for fixed-threshold and z-score classification in src/test/java/com/solidanalysis/scoring/ClassificationServiceTest.java"
Task: "Add unit tests for sanitized output path resolution in src/test/java/com/solidanalysis/scoring/ProjectOutputPathResolverTest.java"

# Launch the core scoring implementation pieces together:
Task: "Implement fixed-threshold and z-score classification in src/main/java/com/solidanalysis/scoring/ClassificationService.java"
Task: "Implement per-class score aggregation and output mapping in src/main/java/com/solidanalysis/scoring/ScoringRunner.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. Stop and validate `--score` independently before moving on

### Incremental Delivery

1. Setup + Foundational => shared scoring scaffolding ready
2. Add User Story 1 => per-class scoring works and can be demoed
3. Add User Story 2 => project summary/ranking becomes available
4. Add User Story 3 => human-readable details and special-case extraction are complete
5. Finish with CLI orchestration, docs, and validation

### Parallel Team Strategy

With multiple developers:

1. One developer can own the foundational readers and path-resolution helpers
2. One developer can implement User Story 1 scoring and output writing
3. One developer can implement User Story 2 summary generation
4. One developer can implement User Story 3 templates and extraction rules
5. A final pass can wire `--all` and update the documentation

---

## Notes

- `[P]` tasks can run in parallel only when they touch different files and do not depend on incomplete work
- `[Story]` labels map each task to a specific user story for traceability
- The `--all` flag is treated as a cross-cutting CLI orchestration feature that composes the existing stage flags in order
- The scoring CLI must fail fast on invalid or incomplete inputs rather than writing partial results

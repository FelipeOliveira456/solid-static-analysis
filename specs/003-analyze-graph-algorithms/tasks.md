---
description: Task list for Graph Algorithms Analysis (Etapa 3)
---

# Tasks: Graph Algorithms Analysis (Etapa 3)

**Input**: Design documents from `/specs/003-analyze-graph-algorithms/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/
**Tests**: For this repository, tests are mandatory for new production code (JUnit 5, under `src/test/java`).
**Organization**: Tasks are grouped by user story (P1 → P2 → P3) so each story can be implemented and tested independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this task belongs to (e.g., `[US1]`, `[US2]`, `[US3]`)
- Every task line MUST include an exact file path in the description.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Create the base packages required by this feature.

- [X] T001 [P] Create production algorithms package `src/main/java/com/solidanalysis/algorithms/package-info.java`
- [X] T002 [P] Create test algorithms package `src/test/java/com/solidanalysis/algorithms/package-info.java`
- [X] T003 Create algorithms subpackages `src/main/java/com/solidanalysis/algorithms/io/GraphDotLoader.java`
- [X] T004 Create algorithms subpackages `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`

---
## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required before any user story implementation.

⚠️ **CRITICAL**: No user story work can begin until this phase is complete.

- [X] T005 Update `pom.xml` to add JGraphT dependencies (core + DOTImporter support via JGraphT IO module) compatible with Java 17 (define a pinned JGraphT version in `pom.xml`)
- [X] T006 Implement CLI entry for `--analyze` by updating `src/main/java/com/solidanalysis/SolidAnalysisCli.java`
- [X] T007 Implement analyze orchestration runner `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java` (detect graphs and call analyzers)
- [X] T008 Implement DOT graph loading for directed/undirected graphs in `src/main/java/com/solidanalysis/algorithms/io/GraphDotLoader.java`
- [X] T009 Implement output directory creation (create `output/<projeto>/algorithms/` if missing) + JSON persistence in `src/main/java/com/solidanalysis/algorithms/io/AlgorithmsJsonWriter.java`
- [X] T010 Define output DTO container classes under `src/main/java/com/solidanalysis/algorithms/model/` (one DTO per required output JSON file)
- [X] T011 Implement graph file discovery and mapping in `src/main/java/com/solidanalysis/algorithms/runners/GraphFileMapper.java`
- [X] T012 Implement consistent failure handling (missing input/invalid DOT) in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsErrorHandler.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

---
## Phase 3: User Story 1 - Analyze graphs and generate algorithms JSON (Priority: P1) 🎯 MVP

**Goal**: When given a valid `output/<projeto>/graphs/` directory, the system produces JSON files under `output/<projeto>/algorithms/` following the required directory and naming conventions, and each output file is valid JSON parseable.

**Independent Test**: Smoke test that validates file presence and JSON parseability for all known graph IDs in the fixture.

### Tests for User Story 1 (required — JUnit 5)

- [X] T020 Unit test `[US1]` smoke-run `--analyze` using a temporary runtime output directory (create `output/<projeto>/algorithms/` under `@TempDir`) and verify JSON presence + parseability in `src/test/java/com/solidanalysis/algorithms/GraphAlgorithmsPipelineSmokeTest.java`

### Implementation for User Story 1

- [X] T021 [US1] Implement default/no-op analyzer wiring (returns empty but valid DTOs) in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`
- [X] T022 [US1] Ensure output JSON file naming + directory structure (create `algorithms/` + subdirs per graph/class/method) in `src/main/java/com/solidanalysis/algorithms/io/AlgorithmsJsonWriter.java`
- [X] T023 [US1] Update DTOs to always serialize to valid JSON even when algorithm results are empty/placeholder in `src/main/java/com/solidanalysis/algorithms/model/`
- [X] T024 [US1] Add detailed warnings when a subset of graphs is missing (do not crash) in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsErrorHandler.java`

**Checkpoint**: User Story 1 should be fully functional and testable independently.

---
## Phase 4: User Story 2 - Detect cycles and compute metrics per graph (Priority: P2)

**Goal**: Provide correct SCC cycle detection, in-degree/out-degree, degree centrality, longest path in DAG, and isolated nodes; and apply those metrics to graphs where they are required (G1/G2/G3/G5/G6/G7).

**Independent Test**: Unit tests for each algorithm on tiny in-memory graphs with known expected values.

### Tests for User Story 2 (required — JUnit 5)

- [X] T030 [US2] SCC detects cycle A→B→A and returns a component with 2 nodes in `src/test/java/com/solidanalysis/algorithms/SccKosarajuAnalyzerTest.java`
- [X] T031 [US2] SCC returns components of size 1 when graph has no cycles in `src/test/java/com/solidanalysis/algorithms/SccKosarajuAnalyzerTest.java`
- [X] T032 [US2] In-degree/out-degree returns correct values for nodes with multiple incoming/outgoing edges in `src/test/java/com/solidanalysis/algorithms/DegreeMetricsAnalyzerTest.java`
- [X] T033 [US2] Degree centrality returns 1.0 for center node in a star graph in `src/test/java/com/solidanalysis/algorithms/DegreeCentralityAnalyzerTest.java`
- [X] T034 [US2] Longest path in DAG returns 3 for A→B→C→D and 1 for single root+child in `src/test/java/com/solidanalysis/algorithms/LongestPathDagAnalyzerTest.java`
- [X] T035 [US2] Isolated nodes detection returns correct set for a directed graph with a node with no edges in `src/test/java/com/solidanalysis/algorithms/IsolatedNodesAnalyzerTest.java`
- [X] T036 [US2] G7 decision-node counting ignores `entry`, `then_`, `else_` but counts `s_` nodes in `src/test/java/com/solidanalysis/algorithms/G7CfgAnalyzerTest.java`
- [X] T037 [US2] G7 longest path with nesting `entry→s_1→then→s_2→then` returns depth 4 in `src/test/java/com/solidanalysis/algorithms/G7CfgAnalyzerTest.java`
- [X] T038 [US2] G7 max decision out-degree (among `s_` nodes only) is computed and serialized correctly in `src/test/java/com/solidanalysis/algorithms/G7CfgAnalyzerTest.java`
- [X] T039 [US2] G6 classifies interface nodes using the known interfaces derived from G5, and computes correct in-degree in `src/test/java/com/solidanalysis/algorithms/G6InterfaceUsageAnalyzerTest.java`

### Implementation for User Story 2

- [X] T040 [US2] Implement SCC (Kosaraju) in `src/main/java/com/solidanalysis/algorithms/algorithms/SccKosarajuAnalyzer.java`
- [X] T041 [US2] Implement in-degree/out-degree mapping for directed graphs in `src/main/java/com/solidanalysis/algorithms/algorithms/DegreeMetricsAnalyzer.java`
- [X] T042 [US2] Implement degree centrality formula for directed graphs in `src/main/java/com/solidanalysis/algorithms/algorithms/DegreeCentralityAnalyzer.java`
- [X] T043 [US2] Implement longest path in DAG using topological iteration (root in-degree zero) in `src/main/java/com/solidanalysis/algorithms/algorithms/LongestPathDagAnalyzer.java`
- [X] T044 [US2] Implement isolated node filtering for directed/undirected in `src/main/java/com/solidanalysis/algorithms/algorithms/IsolatedNodesAnalyzer.java`
- [X] T045 [US2] Implement G7 CFG analysis (decision count + max decision out-degree among `s_` nodes + longest path from `entry` over decision nodes) in `src/main/java/com/solidanalysis/algorithms/algorithms/G7CfgAnalyzer.java`
- [X] T046 [US2] Update graph-level analyzers for G1/G2/G3 to use SCC + degree metrics + isolated nodes in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`
- [X] T047 [US2] Update graph-level analyzers for G5/G6 to derive the known interface set from G5, classify interface nodes in G6 accordingly, then compute in-degree for interfaces and out-degree for classes in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`
- [X] T048 [US2] Update DTO mapping so G1/G2/G3/G5/G6/G7 JSONs include computed metrics (not placeholders) in `src/main/java/com/solidanalysis/algorithms/model/`

**Checkpoint**: US2 algorithms should pass unit tests independently.

---
## Phase 5: User Story 3 - Cohesion and clustering outputs (Priority: P3)

**Goal**: Compute LCOM for G4 field usage and produce connected-components and Louvain clustering outputs for the required graphs.

**Independent Test**: Unit tests for LCOM, connected components, and Louvain clustering on minimal graphs with known expected results.

### Tests for User Story 3 (required — JUnit 5)

- [X] T050 [US3] LCOM returns 1.0 for two methods with completely disjoint attribute sets, and also verifies G4 field out-degree (m_) and in-degree (f_) mappings in `src/test/java/com/solidanalysis/algorithms/LcomAnalyzerTest.java`
- [X] T051 [US3] LCOM returns 0.0 when two methods access exactly the same attributes in `src/test/java/com/solidanalysis/algorithms/LcomAnalyzerTest.java`
- [X] T052 [US3] LCOM returns 0.5 for three methods where one pair shares an attribute and other pairs do not in `src/test/java/com/solidanalysis/algorithms/LcomAnalyzerTest.java`
- [X] T053 [US3] Connected components separates two groups without edges between them and identifies isolated nodes in projection graphs in `src/test/java/com/solidanalysis/algorithms/ConnectedComponentsAnalyzerTest.java`
- [X] T054 [US3] Louvain separates two densily-connected groups with few edges between them and is reproducible under fixed seed in `src/test/java/com/solidanalysis/algorithms/LouvainClusteringAnalyzerTest.java`

### Implementation for User Story 3

- [X] T055 [US3] Implement LCOM calculation on G4 field usage graph with `m_` methods and `f_` attributes, including computation/persistence of out-degree (m_) and in-degree (f_) in `src/main/java/com/solidanalysis/algorithms/algorithms/LcomAnalyzer.java`
- [X] T056 [US3] Implement connected components extraction for SimpleGraph (projection) and ensure isolated nodes list is produced for G4 projection in `src/main/java/com/solidanalysis/algorithms/algorithms/ConnectedComponentsAnalyzer.java`
- [X] T057 [US3] Implement Louvain clustering in `src/main/java/com/solidanalysis/algorithms/algorithms/LouvainClusteringAnalyzer.java` (G1/G3/G4 projection via grafo nao-dirigido ponderado; campo JSON `clusters`)
- [X] T058 [US3] Update graph-level analyzers for G4 field usage and G4 projection to use (1) field degrees (m_/f_) + LCOM for field usage and (2) isolated nodes + connected components + Louvain outputs for projection in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`
- [X] T059 [US3] Update graph-level analyzers for Louvain for G1/G3/G4 projection (not G7) in `src/main/java/com/solidanalysis/algorithms/runners/GraphAlgorithmsRunner.java`
- [X] T060 [US3] Update DTO mapping for G4 field degrees + G4 projection isolated nodes + clustering outputs in `src/main/java/com/solidanalysis/algorithms/model/`

**Checkpoint**: US3 algorithms should pass unit tests independently.

---
## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories.

- [X] T070 Polish: add Javadoc for all public classes/methods in `src/main/java/com/solidanalysis/algorithms/`
- [X] T071 Polish: ensure determinism (stable ordering in JSON) in `src/main/java/com/solidanalysis/algorithms/io/AlgorithmsJsonWriter.java`
- [X] T072 Polish: add integration test using a temporary output directory (@TempDir); copy DOT fixtures from `src/test/resources/java-fixtures/output/graphs/` into `output/<projeto>/graphs/` and verify JSONs are produced under `output/<projeto>/algorithms/` (algorithms/ dir must be created at runtime) in `src/test/java/com/solidanalysis/algorithms/GraphAlgorithmsIntegrationFixturesTest.java`
- [X] T073 Polish: ensure CLI returns non-zero exit code on invalid input directory in `src/main/java/com/solidanalysis/SolidAnalysisCli.java`
- [X] T074 Final: run `mvn test` and fix any failing tests (no new tasks) in `pom.xml`
- [X] T075 Polish: update `ITERACAO.md` with a short “Iteração 003 — Algoritmos de Grafos” summary at the end of implementation (context for how to execute/test this etapa) in `ITERACAO.md`
- [X] T076 Polish: add full-fixture integration tests: (1) all `java-fixtures/output/graphs/` without clustering in `GraphAlgorithmsJavaFixturesFullIntegrationTest.java`; (2) same tree with Louvain in `GraphAlgorithmsJavaFixturesClusteringIntegrationTest.java` (`@Tag("clustering")`, opcional no Surefire padrao)

---
## Dependencies & Execution Order

- Phase 1 has no dependencies.
- Phase 2 must complete before US1/US2/US3.
- US1/US2/US3 are independent after Phase 2 (but they share the runner, DTOs and DOT loader).


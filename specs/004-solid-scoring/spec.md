# Feature Specification: ETAPA 4 - Scoring SOLID

**Feature Branch**: `[004-solid-scoring]`  
**Created**: 2026-03-21  
**Status**: Draft  
**Input**: User description: "ETAPA 4 - SCORING SOLID"

**Constitution**: Feature branch naming, testing, and layout MUST align with
`.specify/memory/constitution.md` (e.g. `NNN-kebab-case`, JUnit 5, Maven `src/main` / `src/test`).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generate class scores (Priority: P1)

As a user running the analysis pipeline, I want the scorer to read the project
results and produce a SOLID score for each class so that I can identify which
classes deserve attention first.

**Why this priority**: Producing per-class scores is the core value of the
feature and is required before any summary or ranking is useful.

**Independent Test**: Can be fully tested by running the scorer against a valid
project output directory and verifying that one JSON file is produced per class
with scores for S, O, L, I, and D.

**Acceptance Scenarios**:

1. **Given** a valid output directory containing algorithms, graphs, and AST
   files, **When** the scorer runs, **Then** it creates one JSON file per class
   inside `scoring/`.
2. **Given** a class with indicators for at least one SOLID principle, **When**
   the score is calculated, **Then** the principle score equals the worst
   classification among its indicators.

---

### User Story 2 - Produce project summary (Priority: P2)

As a user reviewing a project, I want a consolidated project summary so that I
can quickly understand the overall SOLID risk and the most affected principle.

**Why this priority**: The summary makes the per-class results actionable and
supports quick prioritization across the whole project.

**Independent Test**: Can be fully tested by running the scorer on a valid
project and verifying `project_summary.json`, including the distribution by
principle, the ranking, and the most violated principle.

**Acceptance Scenarios**:

1. **Given** a project with multiple classes and mixed scores, **When** the
   summary is generated, **Then** it includes a ranking ordered by overall risk.
2. **Given** several classes that tie on the same overall score, **When** the
   summary is generated, **Then** each class still appears with the correct
   worst principle.

---

### User Story 3 - Explain the score details (Priority: P3)

As a user inspecting a score, I want each result to include human-readable
details for the indicators so that I can understand why a class received a
given score.

**Why this priority**: Explanations are important for interpretation, but they
depend on the scoring itself and can be validated after the core outputs exist.

**Independent Test**: Can be fully tested by checking that each indicator in
the JSON output includes a filled `detail` message that matches the expected
template.

**Acceptance Scenarios**:

1. **Given** an indicator value and template, **When** the result is emitted,
   **Then** the detail text interpolates the values correctly.
2. **Given** a class with no applicable indicator for a principle, **When** the
   class output is generated, **Then** the principle still has a valid score
   derived from the remaining indicators.

---

### Edge Cases

- A project with fewer than 10 classes uses fixed thresholds rather than
  z-score classification.
- A project with 10 or more classes uses z-score classification, and equal
  values with zero standard deviation are classified as BAIXO.
- A valid output directory is missing one of the expected algorithm files or
  graph files, in which case the scorer should fail with a clear error message
  instead of writing partial results.
- A class has no indicators for a principle because the corresponding data is
  absent, in which case the class output still includes the principle with the
  best available classification from the remaining indicators.
- A class is abstract, in which case it must not receive the concrete-extends
  indicator.
- Graph data and algorithm data disagree on a class name, in which case the
  scorer should ignore unmatched entries rather than inventing a score.

## Assumptions

- The scorer receives the absolute path to the already-generated project output
  directory and writes results inside the matching `scoring/` folder.
- The original project path is preserved in each JSON output as `projectPath`.
- The sanitized project directory name is derived from the absolute path by
  replacing path separators with underscores.
- Threshold values for the fixed strategy are configurable through
  `analysis.properties` in the project root, with the documented defaults used
  when no override is present.
- The score order is `BAIXO` < `MEDIO` < `ALTO`, and the worst score always wins
  when combining indicators or principles.
- When multiple indicators in the same principle produce the same worst score,
  all of them are retained in the output for traceability.
- `--all` is a convenience orchestration mode, not a new analysis algorithm; it
  simply composes the existing stage commands in order.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST accept a project output directory and derive the
  sanitized project identifier from its absolute path.
- **FR-002**: The system MUST read algorithm outputs from `algorithms/`, graph
  data from `graphs/`, and root AST JSON files from the project output root.
- **FR-003**: The system MUST generate one JSON score file per class under
  `scoring/` and a single `project_summary.json` file for the project.
- **FR-004**: Each class score MUST include the class name, original project
  path, a score for each SOLID principle, and an overall score.
- **FR-005**: Each principle score MUST be computed from all applicable
  indicators for that principle, using the worst classification among them.
- **FR-006**: The scoring strategy MUST use fixed thresholds when the project
  has fewer than 10 classes and z-score classification when the project has 10
  or more classes.
- **FR-007**: Fixed thresholds for each metric MUST be loaded from
  `analysis.properties`, while documented defaults MUST be used when no custom
  value is provided.
- **FR-008**: For z-score classification, values below 1.0 MUST be classified
  as BAIXO, values from 1.0 up to but not including 2.0 MUST be classified as
  MEDIO, and values 2.0 or above MUST be classified as ALTO.
- **FR-009**: When the standard deviation for a metric is zero, all values for
  that metric MUST be classified as BAIXO.
- **FR-010**: The S principle MUST consider the LCOM value, the number of
  projection clusters, and the proportion of isolated G3 methods.
- **FR-011**: The O principle MUST consider switch-case concentration and
  inheritance from concrete superclasses.
- **FR-012**: The L principle MUST consider hierarchy depth and in-degree of
  concrete classes.
- **FR-013**: The I principle MUST consider interface implementation counts and
  zero in-degree interfaces from both interface-usage and implementation views.
- **FR-014**: The D principle MUST consider direct instantiations, normalized
  out-degree, and the proportion of concrete dependencies.
- **FR-015**: Each indicator MUST include a template identifier, the raw value
  used for scoring, and a human-readable detail string.
- **FR-016**: Indicator detail strings MUST interpolate values using the
  documented templates for the corresponding metric.
- **FR-017**: The system MUST exclude concrete-extends indicators for classes
  marked as abstract in the AST data.
- **FR-018**: The system MUST extract class-specific G6 usage data from the DOT
  graph representation when no class-level algorithm output exists.
- **FR-019**: The project summary MUST include the distribution of classes by
  principle and score level, the most violated principle, and a ranking of
  classes by overall score.
- **FR-020**: The ranking MUST list classes from worst overall score to best
  overall score, preserving a deterministic order for ties.
- **FR-021**: Missing or inconsistent input data MUST result in a clear failure
  message instead of silently producing incomplete scoring files.
- **FR-022**: The CLI MUST provide a `--all` mode that runs scanning, graph
  generation, algorithm analysis, and scoring in sequence for an absolute
  project root, producing the same stage outputs as running each step
  separately.

### Key Entities *(include if feature involves data)*

- **Project Score**: Consolidated assessment for one project output directory,
  including classification strategy, ranking, and principle distribution.
- **Class Score**: Score record for one class, containing the per-principle
  scores, indicator details, and the overall result.
- **Indicator**: A single measurable signal used to classify a class under one
  SOLID principle.
- **Principle Score**: The aggregated score for one SOLID principle derived
  from one or more indicators.
- **Threshold Configuration**: User-configurable numeric boundaries that define
  how fixed-threshold classification maps values to BAIXO, MEDIO, and ALTO.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A valid project output directory produces one score file per
  class and one project summary file in the expected `scoring/` folder on the
  first run.
- **SC-002**: At least 95% of indicator details in generated score files match
  their expected human-readable template exactly for a representative fixture
  set.
- **SC-003**: A project with fewer than 10 classes classifies a value of 0.5
  for LCOM as MEDIO when fixed thresholds are used.
- **SC-004**: A project with 10 or more classes classifies an outlier more than
  2 standard deviations above the mean as ALTO.
- **SC-005**: The project summary reports the correct most violated principle
  and ranking order for a representative multi-class fixture.
- **SC-006**: The scorer completes without manual intervention for valid input
  directories and fails fast with a readable error when required inputs are
  missing.

# Data Model: ETAPA 4 - Scoring SOLID

## Overview

The scorer consumes project artifacts from `output/<project>/` and produces two
output document types: one JSON file per class and a consolidated project
summary.

## Entities

### ScoringInput

| Field | Type | Description |
|---|---|---|
| `projectPath` | string | Absolute path to the project output directory received by `--score` |
| `sanitizedProjectName` | string | Output directory name derived from the absolute path |
| `classCount` | integer | Number of classes discovered from the available artifacts |
| `classificationStrategy` | enum | `FIXED_THRESHOLD_RELAXED` when `classCount < 10` (fixed bands with `f(n)=n/(n+k)` on selected metrics), otherwise `Z_SCORE` |
| `relaxFactor` | number | `f(n)=n/(n+k)` for the run (also on each class JSON); `1.0` when `k=0` |

**Validation**

- `projectPath` must be absolute.
- Required input folders and root JSON files must exist before scoring starts.
- `sanitizedProjectName` must be deterministic for the same absolute path.

### ThresholdConfiguration

| Field | Type | Description |
|---|---|---|
| `lcomMedium` | number | Fixed threshold separating BAIXO and MEDIO for LCOM |
| `lcomHigh` | number | Fixed threshold separating MEDIO and ALTO for LCOM |
| `projectionClusterMedium` | integer | Threshold for 2-cluster MEDIO classification |
| `projectionClusterHigh` | integer | Threshold for 3+ cluster ALTO classification |
| `isolatedRatioMedium` | number | Ratio threshold for MEDIO isolated methods |
| `isolatedRatioHigh` | number | Ratio threshold for ALTO isolated methods |
| `switchCasesMedium` | integer | Threshold for MEDIO switch fan-out |
| `switchCasesHigh` | integer | Threshold for ALTO switch fan-out |
| `depthMedium` | integer | Threshold for MEDIO inheritance depth |
| `depthHigh` | integer | Threshold for ALTO inheritance depth |
| `inDegreeMedium` | integer | Threshold for MEDIO concrete in-degree |
| `inDegreeHigh` | integer | Threshold for ALTO concrete in-degree |
| `classOutDegreeMedium` | integer | Threshold for MEDIO interface implementation count |
| `classOutDegreeHigh` | integer | Threshold for ALTO interface implementation count |
| `instantiationMedium` | integer | Threshold for MEDIO direct instantiation count |
| `instantiationHigh` | integer | Threshold for ALTO direct instantiation count |
| `normalizedOutDegreeMedium` | number | Threshold for MEDIO normalized out-degree |
| `normalizedOutDegreeHigh` | number | Threshold for ALTO normalized out-degree |
| `concreteDependencyMedium` | number | Threshold for MEDIO concrete dependency ratio |
| `concreteDependencyHigh` | number | Threshold for ALTO concrete dependency ratio |
| `g1OutCentralityMedium` | number | MEDIO band lower bound for G1 out-centrality |
| `g1OutCentralityHigh` | number | ALTO band lower bound for G1 out-centrality |
| `relaxK` | integer | Denominator constant `k` in `f(n)=n/(n+k)` (`scoring.relax.k`) |

**Validation**

- Missing values fall back to documented defaults.
- Non-numeric values are invalid and must fail the run.

### IndicatorResult

| Field | Type | Description |
|---|---|---|
| `templateId` | string | Identifier of the interpolated template |
| `value` | number | Raw value used to classify the indicator |
| `detail` | string | Human-readable explanation written to the output JSON |

**Relationships**

- Each `IndicatorResult` belongs to exactly one `PrincipleScore`.

### PrincipleScore

| Field | Type | Description |
|---|---|---|
| `principle` | enum | One of `S`, `O`, `L`, `I`, `D` |
| `score` | enum | `BAIXO`, `MEDIO`, or `ALTO` |
| `indicators` | list | All indicator results that contributed to the principle |

**Validation**

- The principle score is the worst classification among its indicators.
- Empty indicator lists are only valid when no applicable signal exists; in
  that case the implementation must still produce a deterministic fallback
  score derived from available data.

### ClassScore

| Field | Type | Description |
|---|---|---|
| `class` | string | Class name being scored |
| `classificationStrategy` | enum | Same as project summary for this run |
| `projectPath` | string | Original absolute project path |
| `relaxFactor` | number | `f(n)` for this run |
| `scores` | map | Per-principle `PrincipleScore` objects keyed by `S`, `O`, `L`, `I`, `D` |
| `overall` | enum | Worst score across the five principles |

**Relationships**

- One `ClassScore` belongs to one `ScoringInput`.
- One `ClassScore` contains exactly five principle entries.

**Validation**

- Abstract classes must not include the `EXTENDS_CONCRETE` indicator.
- The overall score must be the worst among the principle scores.

### ProjectSummary

| Field | Type | Description |
|---|---|---|
| `projectPath` | string | Original absolute project path |
| `classificationStrategy` | enum | Strategy used for the whole run |
| `relaxFactor` | number | `f(n)=n/(n+k)` for this project |
| `mostViolatedPrinciple` | enum or null | Principle with the largest number of `ALTO` class scores |
| `principleDistribution` | object | Counts of `ALTO`, `MEDIO`, and `BAIXO` per principle |
| `ranking` | list | Ordered list of class outcomes |

### RankingEntry

| Field | Type | Description |
|---|---|---|
| `class` | string | Class name |
| `overall` | enum | Overall class score |
| `worst` | enum or null | Principle that produced the worst score, if any |

**Validation**

- Ranking must be sorted from worst to best overall score.
- Ties must be resolved deterministically by class name.

## Relationships Summary

- `ScoringInput` drives the entire scoring run.
- `ThresholdConfiguration` is consumed when the strategy is
  `FIXED_THRESHOLD_RELAXED` (small projects) or indirectly for defaults; large
  projects use `Z_SCORE` for the same raw metrics where applicable.
- `ClassScore` aggregates five `PrincipleScore` values.
- Each `PrincipleScore` aggregates one or more `IndicatorResult` values.
- `ProjectSummary` aggregates all generated `ClassScore` objects.

## State Notes

This feature does not introduce persistent domain state. All model objects are
derived from the input files on each run and written back as JSON outputs.

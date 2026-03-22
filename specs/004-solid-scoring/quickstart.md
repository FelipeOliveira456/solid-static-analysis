# Quickstart: ETAPA 4 - Scoring SOLID

## Prerequisites

- Java 17
- Maven
- A project output directory containing:
  - root AST JSON files
  - `graphs/`
  - `algorithms/`

## Build

```bash
mvn clean package
```

## Run Tests

```bash
mvn test
```

## Run the Scorer

```bash
java -jar target/solid-static-analysis.jar --score /absolute/path/to/output/project
```

## Run the Full Pipeline

```bash
java -jar target/solid-static-analysis.jar --all /absolute/path/to/project/root
java -jar target/solid-static-analysis.jar --all /absolute/path/to/project/root --output /absolute/path/to/project-output
```

The scorer writes results to:

```text
/absolute/path/to/output/project/scoring/
```

Expected files:

- one `<Class>.json` file per class
- one `project_summary.json`

For `--all`, the scanner, graph generation, analysis, and scoring stages run
in sequence and produce the same stage outputs as running them individually.
Outputs are written under `{user.dir}/output/<sanitized-absolute-root>/`, where
`<sanitized-absolute-root>` is derived from the project path (see
`ProjectOutputPathResolver` in the codebase).

## Example Using the Repository Fixtures

```bash
java -jar target/solid-static-analysis.jar \
  --score /home/felipe/Documents/AS/solid-static-analysis/src/test/resources/java-fixtures/output
```

## Optional Threshold Overrides

Default thresholds ship in the JAR as `/analysis.properties` (same keys as the
repo-root file). Optional `analysis.properties` in the process working
directory merges on top (only defined keys override).

Small projects (`classCount < 10`) use `FIXED_THRESHOLD_RELAXED`: selected
continuous metrics use `f(n)=n/(n+k)` with `scoring.relax.k` in root
`analysis.properties`. Set `k=0` for no scaling (`f=1`). Each JSON output
includes `relaxFactor` (`f(n)`).

Isolated methods (G3 / SRP) use `effectiveRatio = rawRatio × C(n,2)/(C(n,2)+k₂)`
with `k₂` from `scoring.isolatedMethods.combinations.k` (larger `k₂` damps small
classes more).

`DISPATCH_AST_HEURISTICS` (OCP, AST): `scoring.dispatchAst.h1.homogeneityThreshold`
for `H = 1 − d/n`; `scoring.dispatchAst.h2.minChainIfs` and
`scoring.dispatchAst.h2.minConsecutiveTopIfs` for branch-volume (H2).

## Verify the Output

1. Confirm that `scoring/` exists.
2. Confirm that each JSON file parses successfully.
3. Confirm that `project_summary.json` includes the ranking and principle
   distribution.

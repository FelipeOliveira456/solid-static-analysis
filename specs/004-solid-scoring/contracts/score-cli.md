# CLI Contract: `--score`

## Command

```bash
java -jar solid-static-analysis.jar --score <ABS_PROJECT_OUTPUT_DIR>
```

```bash
java -jar solid-static-analysis.jar --all <ABS_PROJECT_ROOT_DIR>
```

```bash
java -jar solid-static-analysis.jar --all <ABS_PROJECT_ROOT_DIR> --output <ABS_PROJECT_OUTPUT_DIR>
```

## Purpose

Read an existing project output directory and generate SOLID scoring results
under its `scoring/` subdirectory.

`--all` runs the full pipeline in stage order from an absolute project root.

## Input Contract

- `<ABS_PROJECT_OUTPUT_DIR>` must be an absolute path.
- The directory must already exist.
- The directory must contain:
  - root AST JSON files from Stage 1
  - `graphs/` from Stage 2
  - `algorithms/` from Stage 3
- Bundled `/analysis.properties` on the classpath supplies all thresholds; an
  optional `analysis.properties` in the working directory merges overrides
  (including `scoring.relax.k` for `f(n)=n/(n+k)`).

For `--all`:

- `<ABS_PROJECT_ROOT_DIR>` must be an absolute path.
- The root must be a valid source project that can be scanned to generate the
  Stage 1 output before the later stages run.
- `--output` is optional and, when present, must be an absolute path where all
  stage outputs are written.

## Output Contract

- `scoring/<Class>.json` for each class discovered in the project
- `scoring/project_summary.json` for the consolidated project score
- Each scoring JSON includes `classificationStrategy` (`FIXED_THRESHOLD_RELAXED`
  when `classCount < 10`, else `Z_SCORE`) and `relaxFactor` (`f(n)`).

For `--all`, the same stage outputs are produced in sequence before scoring.
Without `--output`, the default location is `{user.dir}/output/<sanitized-root>/`.

## Exit Behavior

- `0` when scoring succeeds and all outputs are written
- `1` when the input path is invalid, required inputs are missing, or scoring
  fails before completing

For `--all`, exit `0` only when all stages complete successfully.

## Output Guarantees

- Output ordering must be deterministic for the same input directory.
- The scorer must not write partial results when required inputs are missing or
  inconsistent.
- Indicator details must always be present and human-readable.

## Error Cases

- Non-absolute path: reject immediately
- Missing `graphs/` or `algorithms/`: fail fast
- Invalid JSON or unreadable DOT content: fail fast
- Non-numeric threshold override: fail fast

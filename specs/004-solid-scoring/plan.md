# Implementation Plan: ETAPA 4 - Scoring SOLID

**Branch**: `[004-solid-scoring]` | **Date**: 2026-03-21 | **Spec**: [`spec.md`](./spec.md)
**Input**: Feature specification from `/specs/004-solid-scoring/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Add new `--score` and `--all` CLI modes. `--score` reads a project output
directory, combines the Stage 1 AST JSON, Stage 2 DOT graphs, and Stage 3
algorithm JSON, and writes per-class SOLID scores plus a project summary under
`scoring/`. `--all` orchestrates the existing pipeline stages in order from an
absolute project root. The scorer will use fixed thresholds for small projects,
z-scores for larger projects, and human-readable indicator templates for every
score detail.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**: Jackson Databind, JGraphT, JUnit 5, Maven, `java.util.Properties`  
**Storage**: File-based inputs and outputs under `output/<path-sanitized>/`; configuration from root `analysis.properties`  
**Testing**: JUnit 5  
**Target Platform**: Linux command-line execution  
**Project Type**: CLI  
**Performance Goals**: Score representative fixture projects in a few seconds and keep output ordering deterministic across runs  
**Constraints**: Absolute path input only; fail fast on missing or inconsistent inputs; do not emit partial scoring output; `--all` must compose the existing stage commands rather than introduce a separate analysis path  
**Scale/Scope**: Single-project output directories with tens of classes and multiple graph/algorithm artifacts

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verificar conformidade com `.specify/memory/constitution.md` (Solid Static Analysis):

- **Branch**: `004-solid-scoring` segue `NNN-nome-curto`, está isolada da mainline e não mistura escopo de outras iterações.
- **Estrutura**: a feature stays dentro do layout Maven existente (`src/main/java`, `src/test/java`, `pom.xml`) e cria apenas `com.solidanalysis.scoring`.
- **Código**: identifiers in English, Javadoc required on public APIs, and small cohesive methods are expected for the new scorer package.
- **Testes**: JUnit 5 for all new code, with isolated tests using synthetic data and mirrored package structure under `src/test/java/com/solidanalysis/scoring`.
- **Documentação da iteração**: the branch must include `ITERACAO.md` when implementation starts; plan artifacts live under `specs/004-solid-scoring/`.
- **Commits (durante implementação)**: commit messages in Portuguese, cohesive and small, per constitution.

No constitution exceptions are required for this plan.

## Project Structure

### Documentation (this feature)

```text
specs/004-solid-scoring/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
└── contracts/
    └── score-cli.md
```

### Source Code (repository root)

```text
src/
├── main/
│   └── java/
│       └── com/
│           └── solidanalysis/
│               ├── scanner/
│               ├── graphs/
│               ├── algorithms/
│               └── scoring/
└── test/
    └── java/
        └── com/
            └── solidanalysis/
                └── scoring/

output/
└── <path-sanitized>/
    └── scoring/
        ├── <Class>.json
        └── project_summary.json
```

**Structure Decision**: Keep the implementation as a single Maven CLI project
and add a dedicated scoring package under the existing `com.solidanalysis`
namespace. The CLI entrypoint stays in `SolidAnalysisCli`; `--score` and
`--all` are routed there, while the scoring logic, configuration loader,
indicators, and writers live in `com.solidanalysis.scoring`.

## Complexity Tracking

No constitutional violations require extra justification.

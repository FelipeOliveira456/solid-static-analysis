# Implementation Plan: Benchmarks e Documentos Human-Readable de Scoring

**Branch**: `005-add-solid-benchmarks-docs` | **Date**: 2026-03-21 | **Spec**: [`spec.md`](./spec.md)
**Input**: Feature specification from `/specs/005-add-solid-benchmarks-docs/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary
Adicionar um diretório `benchmarks/` com casos “bad” e “good” (e `bad-good-cases/` como referência) para validação do scorer de SOLID, e estender o modo `--score` para gerar documentos human-readable em `output/<path-sanitizado>/results/`: um `.txt` por classe e um `project_summary.txt`, derivados dos JSONs já produzidos em `scoring/`.

## Technical Context

<!-- Technical context filled in for this feature; template guidance removed. -->

**Language/Version**: Java 17  
**Primary Dependencies**: Jackson Databind (leitura dos JSONs) e JUnit 5 (testes)  
**Storage**: File-based inputs and outputs under `output/<path-sanitized>/` (incluindo `scoring/` e `results/`) e diretório `benchmarks/` no repo root  
**Testing**: JUnit 5  
**Target Platform**: Linux command-line execution  
**Project Type**: CLI  
**Performance Goals**: Gerar `results/` para dezenas de classes mantendo ordenação determinística em tempo razoável  
**Constraints**: Não emitir arquivos parciais em caso de inputs incompletos; manter formato textual estável e idempotente (reexecuções substituem/atualizam)  
**Scale/Scope**: Diretorios de saída com dezenas de classes e artefatos JSON por classe

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verificar conformidade com `.specify/memory/constitution.md` (Solid Static Analysis):

- **Branch**: nome da feature branch segue `NNN-nome-curto` (sequencial a partir de `main`); sem
  mistura de escopo de outras iterações; sem entrega direta em `main` fora do fluxo acordado.
- **Estrutura**: layout Maven (`src/main/java`, `src/test/java`, `pom.xml`); pacote base
  `com.solidanalysis` salvo exceção documentada no plano.
- **Código**: identificadores em inglês; Javadoc em toda classe pública e método público;
  comentários onde a lógica não for óbvia; métodos pequenos e coesos.
- **Testes**: JUnit 5; testes para código novo; independência (sem rede, sem arquivos externos,
  dados sintéticos); nomes de teste descritivos; espelhamento `src/test` → `src/main`.
- **Documentação da iteração**: `ITERACAO.md` na raiz da branch; ao integrar em `main`, entrada
  correspondente no `CHANGELOG.md`; `README.md` reflete build/execução do estado de `main`.
- **Commits (durante implementação)**: mensagens em português, coesas e pequenas.

No constitution exceptions are required for this plan.

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
```text
src/
└── main/java/com/solidanalysis/scoring/
    ├── (novo) HumanReadableResultsWriter.java
    └── (novo/atualizado) ligação no ScoringRunner para gerar `results/`

src/
└── test/java/com/solidanalysis/
    ├── SolidAnalysisCliTest.java (smoke tests para `--score`)
    └── scoring/
        ├── HumanReadableResultsWriterTest.java
        └── ProjectSummaryTxtFormatterTest.java

benchmarks/
├── bad-good-cases/
├── bad-project/
└── good-project/

output/                         # gerado em runtime durante execução/testes
└── <path-sanitized>/results/
    ├── <Classe>.txt
    └── project_summary.txt
```

**Structure Decision**: estender a etapa 4 adicionando um writer dedicado a converter `scoring/*.json` para `results/*.txt`, mantendo o CLI atual como “orquestrador” e versionando apenas os benchmarks como artefatos de dados no repo root.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

N/A (no constitutional complexity exceptions are expected for this feature).

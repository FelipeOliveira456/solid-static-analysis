# Implementation Plan: Graph Algorithms Analysis (Etapa 3)

**Branch**: `003-analyze-graph-algorithms` | **Date**: 2026-03-21 | **Spec**: [`spec.md`](./spec.md)
**Input**: Feature specification from `/specs/003-analyze-graph-algorithms/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Esta etapa implementa a fase de “algoritmos de grafos” do analisador: carregar os grafos `.dot` gerados na Etapa 2 usando JGraphT, executar os algoritmos definidos (SCC, graus e centralidade, longest path em DAG, nós isolados, componentes conectados, LCOM e Louvain) e persistir os resultados em JSON no diretório `algorithms/` de cada projeto de saída.

## Technical Context

<!-- Conteudo preenchido para a Etapa 3 (algoritmos de grafos). -->

**Language/Version**: Java 17 (Maven; source/target 17)
**Primary Dependencies**: JGraphT (DOTImporter e algoritmos) + Jackson (JSON) + JUnit 5 (testes)
**Storage**: Disco via JSON em `output/<projeto>/algorithms/` e leitura de DOT em `output/<projeto>/graphs/`
**Testing**: JUnit 5
**Target Platform**: Execucao local como CLI em ambiente Linux
**Project Type**: CLI (jar) de analise estatico baseada em artefatos de grafos
**Performance Goals**: Concluir analise de fixtures pequenas/medias em ate 30 segundos em ambiente de desenvolvimento
**Constraints**: Saida deterministica; sem dependencias de rede; comportamento consistente ao lidar com entradas ausentes/invalidas
**Scale/Scope**: Tamanho dos grafos proporcional ao tamanho do projeto analisado (classes, metodos, campos e controle de fluxo), limitado pelos grafos `.dot` gerados na Etapa 2.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Verificar conformidade com `.specify/memory/constitution.md` (Solid Static Analysis):

- **Branch**: PASS (branch `003-analyze-graph-algorithms` no formato `NNN-nome-curto`).
- **Estrutura**: PASS (layout Maven ja existente; novo codigo em `src/main/java/com/solidanalysis/algorithms/` e testes em `src/test/java/com/solidanalysis/algorithms/`).
- **Código**: PASS (seguiremos: identificadores em inglês, Javadoc para API publica, comentarios para logica nao trivial e metodos pequenos/coesos).
- **Testes**: PASS (JUnit 5; testes unitarios com grafos em memoria; testes de integracao com fixtures em `src/test/resources`).
- **Documentação da iteração**: PASS (`ITERACAO.md` e `CHANGELOG.md` ja existem na raiz do repositório; serao atualizados/estendidos durante a implementacao e integracao).
- **Commits (durante implementação)**: PASS (mensagens em português, coesas e pequenas).
- Re-check post Phase 1 design: PASS (pesquisa e data-model/contratos/quickstart criados sem novas exigencias).

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
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
# [Novo] Etapa 3: algoritmos e testes unitarios/de integracao
src/
└── main/java/com/solidanalysis/
    ├── scanner/        (Etapa 1 - existe)
    ├── graphs/         (Etapa 2 - existe)
    └── algorithms/    (Etapa 3 - novo)

src/
└── test/java/com/solidanalysis/
    └── algorithms/   (testes unitarios e integracao - novo)

output/
└── <nome-do-projeto>/
    └── algorithms/  (saida JSON gerada em runtime)
```

**Structure Decision**: Implementar a Etapa 3 em `com.solidanalysis.algorithms` para manter separacao entre responsabilidades (scanner/graphs/algorithms). Atualizar o CLI (`com.solidanalysis.SolidAnalysisCli`) para suportar `--analyze <output/<projeto>>`, carregando grafos de `output/<projeto>/graphs/` e gravando resultados em `output/<projeto>/algorithms/` com convencoes por grafo/classe/metodo.

## Complexity Tracking

N/A (Constitution Check passa sem violacoes que exijam justificativa).

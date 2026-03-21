---
description: "Task list for 002-generate-dot-graphs — DOT graphs from AST JSON"
---

# Tasks: Geração de grafos DOT (002)

**Input**: Design documents from `/home/felipe/Documents/AS/solid-static-analysis/specs/002-generate-dot-graphs/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [research.md](./research.md), [data-model.md](./data-model.md), [contracts/](./contracts/)

**Tests**: Obrigatórios (JUnit 5, `src/test/java`) para código novo; dados sintéticos em memória, sem
JSON de fixtures versionados; `@TempDir` permitido para `.dot` escritos pelo SUT ou diretório de
projeto vazio (ver [spec.md](./spec.md) FR-012).

**Organization**: Fases por user story da [spec.md](./spec.md) (P1 → P3), com fundação bloqueante antes das stories.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode correr em paralelo (ficheiros distintos, sem dependência de tarefas incompletas do mesmo grupo)
- **[USn]**: User story da spec (US1, US2, US3)
- Cada descrição inclui caminho de ficheiro concreto

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Estrutura de pacotes e alinhamento Maven para a Etapa 2.

- [x] T001 Add package Javadoc in `src/main/java/com/solidanalysis/graphs/package-info.java`
- [x] T002 [P] Add package Javadoc in `src/test/java/com/solidanalysis/graphs/package-info.java`
- [x] T003 Plan and apply shaded JAR `mainClass` change in `pom.xml` to a single entry point that supports both scan mode and `--graphs` (see [contracts/cli-graph-generation.md](./contracts/cli-graph-generation.md))

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Leitura JSON, modelo em memória, filtros de tipo, emissão DOT e CLI — **obrigatório antes de US1–US3**.

**⚠️ CRITICAL**: Nenhuma story de grafos começa antes desta fase estar completa.

- [x] T004 Implement Jackson DTOs mirroring `specs/001-ast-parser/contracts/ast-artifact.schema.json` under `src/main/java/com/solidanalysis/graphs/model/` (e.g. `AstArtifact.java`, `TypeSummary.java`, `MethodSummary.java`, nested summaries)
- [x] T005 Implement `ProjectJsonLoader` in `src/main/java/com/solidanalysis/graphs/io/ProjectJsonLoader.java` to load all `*.json` from a project output directory
- [x] T006 Implement `ParsedProject` and type/method indexes in `src/main/java/com/solidanalysis/graphs/model/ParsedProject.java` per [data-model.md](./data-model.md)
- [x] T007 Implement `TypeRelevanceFilter` (primitives, `void`, `java.*`, `javax.*`, `System.out` handling) in `src/main/java/com/solidanalysis/graphs/filter/TypeRelevanceFilter.java`
- [x] T008 Implement deterministic DOT serialization helpers in `src/main/java/com/solidanalysis/graphs/dot/DotWriter.java` per [contracts/dot-graph-output.md](./contracts/dot-graph-output.md)
- [x] T009 Implement unified CLI entry (e.g. `src/main/java/com/solidanalysis/SolidAnalysisCli.java`) parsing `--graphs <ABS_DIR>` vs legacy single-arg scan; delegate scan to `ScannerCli.run` (ou equivalente); set `pom.xml` shade `mainClass` to this class (ligação ao `GraphGenerationRunner` fica em T025 após T017)
- [x] T010 Add unit tests for `TypeRelevanceFilter` in `src/test/java/com/solidanalysis/graphs/filter/TypeRelevanceFilterTest.java`
- [x] T011 [P] Add `GraphModelJsonTest.java` in `src/test/java/com/solidanalysis/graphs/model/GraphModelJsonTest.java` — desserialização Jackson dos DTOs com JSON mínimo em string (memória) e campos opcionais omitidos
- [x] T012 [P] Add `ProjectJsonLoaderTest.java` in `src/test/java/com/solidanalysis/graphs/io/ProjectJsonLoaderTest.java` — diretório sem `*.json` e diretório com vários artefactos (JUnit `@TempDir`, sem fixtures versionados)
- [x] T013 [P] Add `ParsedProjectTest.java` in `src/test/java/com/solidanalysis/graphs/model/ParsedProjectTest.java` — construção de índices a partir de listas sintéticas em memória
- [x] T014 [P] Add `DotWriterTest.java` in `src/test/java/com/solidanalysis/graphs/dot/DotWriterTest.java` — escaping de labels/IDs e ordenação determinística

**Checkpoint**: Projeto compila; CLI reconhece `--graphs`; modelo, filtro, loader e emissão DOT testáveis.

---

## Phase 3: User Story 1 — Gerar todos os grafos estruturais (Priority: P1) 🎯 MVP

**Goal**: Dado um diretório com JSONs válidos da Etapa 1, gerar todos os `.dot` em `graphs/` e `graphs/g7_cfg/` conforme [spec.md](./spec.md) e [contracts/dot-graph-output.md](./contracts/dot-graph-output.md).

**Independent Test**: `mvn test` inclui teste com `@TempDir` que corre o runner e verifica existência dos ficheiros esperados; manualmente, `java -jar target/solid-static-analysis.jar --graphs <abs>` produz os mesmos nomes de ficheiro.

### Tests for User Story 1 (required — JUnit 5) ⚠️

> Escrever antes da implementação completa dos geradores; falhar até o runner e geradores existirem.

- [x] T015 [US1] Add `GraphGenerationRunnerTest.java` in `src/test/java/com/solidanalysis/graphs/GraphGenerationRunnerTest.java` asserting creation of `g1_dependency.dot` through `g6_interface_usage.dot` and `g7_cfg/` outputs under a temporary project directory (com `*.json` sintéticos escritos pelo próprio teste, não a partir de recursos versionados)
- [x] T016 [US1] Extend `GraphGenerationRunnerTest.java` — diretório de entrada **sem** ficheiros `*.json` ainda assim cria `graphs/` com DOTs G1–G6 válidos (vazios ou mínimos) e `g7_cfg/` sem crash, alinhado ao edge case da [spec.md](./spec.md)

### Implementation for User Story 1

- [x] T017 [US1] Implement `GraphGenerationRunner` in `src/main/java/com/solidanalysis/graphs/GraphGenerationRunner.java` to create `graphs/` and `graphs/g7_cfg/`, invoke all generators, and write files per [contracts/cli-graph-generation.md](./contracts/cli-graph-generation.md)
- [x] T018 [P] [US1] Implement `G1DependencyGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G1DependencyGraphGenerator.java` (edge labels: `field`, `param`, `return`, `call`, `instantiation`)
- [x] T019 [P] [US1] Implement `G2InheritanceGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G2InheritanceGraphGenerator.java` (labels `extends`, `implements`)
- [x] T020 [P] [US1] Implement `G3MethodCallsGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G3MethodCallsGraphGenerator.java` (method nodes `Class.method`, constructors with `<init>`)
- [x] T021 [P] [US1] Implement `G4FieldUsageGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G4FieldUsageGraphGenerator.java` (`g4_field_usage.dot` + `g4_method_projection.dot`, labels `read`/`write`)
- [x] T022 [P] [US1] Implement `G5InterfaceImplGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G5InterfaceImplGraphGenerator.java` (ellipse vs box)
- [x] T023 [P] [US1] Implement `G6InterfaceUsageGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G6InterfaceUsageGraphGenerator.java`
- [x] T024 [US1] Implement `G7CfgGraphGenerator.java` in `src/main/java/com/solidanalysis/graphs/G7CfgGraphGenerator.java` (per-method `.dot` only when `controlFlowStatements` non-empty; `then`/`else`/`loop`, `chainedElseIf`)
- [x] T025 [US1] In unified CLI (`src/main/java/com/solidanalysis/SolidAnalysisCli.java` ou classe definida em T009), invocar `GraphGenerationRunner` quando `--graphs`; validar path absoluto e códigos de saída (completar o que T009 deixou como delegação)

**Checkpoint**: US1 entregável e testável de forma independente (ficheiros DOT gerados).

---

## Phase 4: User Story 2 — Garantir relevância das dependências (Priority: P2)

**Goal**: Grafos sem ruído de JDK, primitivos e `System.out`, alinhado a FR-010 da spec.

**Independent Test**: Testes unitários em memória verificam ausência de arestas/nós indesejados nos DOT gerados para fixtures sintéticas.

### Tests for User Story 2 (required — JUnit 5) ⚠️

- [x] T026 [P] [US2] Add `G1DependencyGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G1DependencyGraphGeneratorTest.java` asserting no dependencies on `java.*` / `javax.*` / primitives in G1 output for synthetic `ParsedProject`
- [x] T027 [P] [US2] Add `G3MethodCallsGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G3MethodCallsGraphGeneratorTest.java` asserting JDK-declared callees do not create edges (synthetic in-memory model)

### Implementation for User Story 2

- [x] T028 [US2] Audit and harden `TypeRelevanceFilter` across **todos** os geradores: `G1DependencyGraphGenerator.java`, `G2InheritanceGraphGenerator.java` (excluir `superclass` / interfaces JDK), `G3MethodCallsGraphGenerator.java`, `G4FieldUsageGraphGenerator.java`, `G5InterfaceImplGraphGenerator.java` (nós/arestas só para tipos do projeto), `G6InterfaceUsageGraphGenerator.java`, e `G7CfgGraphGenerator.java`, de forma que `System.out` e tipos excluídos nunca apareçam onde a [spec.md](./spec.md) FR-010 exige filtragem

**Checkpoint**: US2 validável sem regressão em US1.

---

## Phase 5: User Story 3 — Validar regras por testes unitários (Priority: P3)

**Goal**: Cobrir os cenários mínimos explícitos na descrição da feature (G2, G4, G5, G6, G7, construtores em G3).

**Independent Test**: `mvn test` cobre herança/implements, acessos a campos, shapes G5, CFG condicional, construtores em G3.

### Tests for User Story 3 (required — JUnit 5) ⚠️

- [x] T029 [P] [US3] Add `G2InheritanceGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G2InheritanceGraphGeneratorTest.java` for correct `extends` / `implements` edges
- [x] T030 [P] [US3] Add `G4FieldUsageGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G4FieldUsageGraphGeneratorTest.java` for correct method–field edges and `read` / `write` labels
- [x] T031 [P] [US3] Add `G5InterfaceImplGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G5InterfaceImplGraphGeneratorTest.java` asserting `shape=ellipse` for interfaces and `shape=box` for classes in DOT output
- [x] T032 [P] [US3] Add `G6InterfaceUsageGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G6InterfaceUsageGraphGeneratorTest.java` — arestas classe → interface a partir de campo, parâmetro ou `declaringType` de chamadas (modelo sintético em memória)
- [x] T033 [US3] Extend `G5InterfaceImplGraphGeneratorTest.java` — projeto só com interfaces (sem classes implementadoras) produz DOT bipartido válido sem arestas de implementação (edge case da [spec.md](./spec.md))
- [x] T034 [P] [US3] Add `G7CfgGraphGeneratorTest.java` in `src/test/java/com/solidanalysis/graphs/G7CfgGraphGeneratorTest.java` asserting no CFG file for methods without control flow and at least one file when control-flow data exists
- [x] T035 [US3] Extend `src/test/java/com/solidanalysis/graphs/G3MethodCallsGraphGeneratorTest.java` to assert constructor (`<init>`) call edges between project methods

**Checkpoint**: Critérios mínimos de teste da spec satisfeitos.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentação da iteração e validação operacional.

- [x] T036 Update root `README.md` with `--graphs` usage, output layout under `output/<project>/graphs/`, and Graphviz preview command
- [x] T037 Add root `ITERACAO.md` describing Etapa 2 scope, run/test instructions per constitution
- [x] T038 [P] Execute manual validation steps from `specs/002-generate-dot-graphs/quickstart.md` after `mvn package` and fix gaps in docs or CLI messages if needed
- [x] T039 Add entrada em `CHANGELOG.md` na raiz do repositório para a Etapa 2 (grafos DOT), alinhado à constituição ao integrar em `main`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1** → **Phase 2** → **Phase 3 (US1)** → **Phase 4 (US2)** → **Phase 5 (US3)** → **Phase 6**
- **US2** depende de geradores US1 existentes para testar DOT; **US3** amplia testes sobre o mesmo código
- **Paralelismo**: dentro da Phase 3, T018–T023 são [P] **após** T017 definir a API do runner
- **T011–T014** após T004–T008 (DTOs, loader, índices, `DotWriter`); **T016** após T015+T017

### User Story Dependencies

| Story | Depends on | Notas |
|-------|------------|--------|
| US1 (P1) | Phase 2 | MVP |
| US2 (P2) | US1 + T007 | Reforço de filtros nos geradores |
| US3 (P3) | US1 (+ T027 para ficheiro G3 partilhado) | T035 edita o mesmo teste que T027; T033 edita `G5InterfaceImplGraphGeneratorTest` |

### Within Each User Story

- Testes listados antes da implementação quando marcado ⚠️; ajustar ordem real se usar TDD estrito por classe geradora
- Geradores US1: modelo comum (`ParsedProject`, `DotWriter`, `TypeRelevanceFilter`) antes de integração no runner

### Parallel Opportunities

- **Phase 1**: T002 [P] em paralelo com T001
- **Phase 2**: DTOs (T004) seguidos de loader/index; T010 e T011–T014 [P] após T004–T008 conforme dependências
- **Phase 3**: T018–T023 [P] [US1] após T017
- **Phase 4**: T026 [P] [US2] e T027 [P] [US2] em paralelo
- **Phase 5**: T029–T032 e T034 [P] [US3] em paralelo; T035 e T033 depois de T027 / T031 respetivamente
- **Phase 6**: T038 [P] em paralelo com revisão de T036–T037 se responsáveis diferentes

---

## Parallel Example: User Story 1

Após T017 (`GraphGenerationRunner` com interface estável):

```text
T018 G1DependencyGraphGenerator.java
T019 G2InheritanceGraphGenerator.java
T020 G3MethodCallsGraphGenerator.java
T021 G4FieldUsageGraphGenerator.java
T022 G5InterfaceImplGraphGenerator.java
T023 G6InterfaceUsageGraphGenerator.java
```

Em paralelo, T015 pode evoluir à medida que cada ficheiro `.dot` é suportado pelo runner.

---

## Implementation Strategy

### MVP First (User Story 1 apenas)

1. Completar Phase 1 e Phase 2  
2. Completar Phase 3 (US1) incluindo T015–T016  
3. Validar com `mvn test` e execução manual `--graphs` num diretório com JSONs reais  

### Incremental Delivery

1. US1 → grafos completos + teste de runner  
2. US2 → testes de exclusão JDK + endurecimento de filtros  
3. US3 → cobertura mínima explícita por grafo  
4. Polish → README, ITERACAO, quickstart, CHANGELOG  

### Parallel Team Strategy

- Após Phase 2: desenvolvedor A em G1/G2/G4, B em G3/G6, C em G5/G7 + runner, sincronizando em `GraphGenerationRunner`  

---

## Task Summary

| Métrica | Valor |
|---------|------:|
| **Total tasks** | 39 |
| Phase 1 (Setup) | 3 |
| Phase 2 (Foundational) | 11 |
| Phase 3 [US1] | 11 |
| Phase 4 [US2] | 3 |
| Phase 5 [US3] | 7 |
| Phase 6 (Polish) | 4 |
| Tasks marked [P] | 24 |

**Suggested MVP scope**: Phase 1 + Phase 2 + Phase 3 (T001–T025).

**Format validation**: IDs T001–T039 contíguos; linhas de tarefa usam `- [ ] Tnnn ...`; fases de user story incluem `[USn]`; caminhos explícitos.

## Notes

- Não modificar a lógica de parsing em `src/main/java/com/solidanalysis/scanner/` salvo o mínimo necessário para o ponto de entrada CLI (T003/T009/T025).  
- IDs DOT e nomes de ficheiro G7 devem seguir [contracts/dot-graph-output.md](./contracts/dot-graph-output.md).  
- Commits em português, pequenos e coesos (constituição).
- **T009 vs T025**: T009 estabelece entry point e `mainClass`; T025 conclui a chamada a `GraphGenerationRunner` quando este existir (T017).

---
description: "Task list for 001-ast-parser (JavaParser scanner + JSON export)"
---

# Tasks: Parser AST (etapa 1)

**Input**: Design documents from `/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/`  
**Prerequisites**: [plan.md](./plan.md), [spec.md](./spec.md), [data-model.md](./data-model.md), [contracts/](./contracts/), [research.md](./research.md)

**Tests**: **Obrigatórios** (spec FR-009 + constituição). Cada **classe pública de produção** MUST ter
classe de teste correspondente (`*Test.java`) em `src/test/java/com/solidanalysis/scanner/` (Princípio IV).
Modelos em `model/` são POJOs — podem ser cobertos indirectamente por testes de `ArtifactJsonWriter` /
`AstExtractor`; se alguma classe de modelo ganhar lógica não trivial, adicionar teste dedicado.

**Organization**: Fases por user story (P1 → P2/P3); fundação técnica + **testes de fundação** antes de US1.

**MVP (T001–T024)**: entrega scan + JSON **mínimo** (tipo nome/kind); **não** satisfaz FR-005 / SC-003 até
completar **US2 (T025–T029)**.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: pode correr em paralelo (ficheiros distintos, sem dependência de tarefas incompletas)
- **[Story]**: [US1]…[US4] nas fases de história; omitir em Setup, Fundação e Polish

## Path Conventions

- Produção: `src/main/java/com/solidanalysis/scanner/`
- Testes: `src/test/java/com/solidanalysis/scanner/`
- Contratos: `specs/001-ast-parser/contracts/`

---

## Phase 1: Setup (infra Maven e árvore)

**Purpose**: Projeto compilável, JAR executável, pastas e **stub** de documentação da constituição.

- [x] T001 Create root `pom.xml` with Java 17, `com.solidanalysis` / `solid-static-analysis`, dependencies `javaparser-core` and `javaparser-symbol-solver-core` **3.26.3**, `jackson-databind` **2.17.2**, `junit-jupiter` **5.10.2**, `maven-compiler-plugin` 17, and `maven-shade-plugin` with `Main-Class` `com.solidanalysis.scanner.ScannerCli` (acceptance: `mvn -q package` produz JAR executável — não é necessária tarefa separada de “wire shade”)
- [x] T002 [P] Create package directories under `src/main/java/com/solidanalysis/scanner/` and `src/main/java/com/solidanalysis/scanner/model/`
- [x] T003 [P] Create package directories under `src/test/java/com/solidanalysis/scanner/`
- [x] T004 [P] Ensure `output/` exists at repo root with `output/.gitkeep` (or documented git policy) per [plan.md](./plan.md)
- [x] T005 [P] Add root `README.md`, `CHANGELOG.md` stubs, and **minimal `ITERACAO.md` stub** (título + branch + “em progresso”) per constitution; expand in T037

---

## Phase 2: Foundational (bloqueante para todas as histórias)

**Purpose**: DTOs, naming de ficheiros, JSON, JavaParser + CombinedTypeSolver, e **testes 1:1** das peças de infra.

**⚠️ CRITICAL**: Nenhuma história de utilizador até completar implementação **e** testes T015–T017 desta fase.

- [x] T006 [P] Add `AstArtifact.java` in `src/main/java/com/solidanalysis/scanner/model/AstArtifact.java` matching [data-model.md](./data-model.md)
- [x] T007 [P] Add `TypeSummary.java` in `src/main/java/com/solidanalysis/scanner/model/TypeSummary.java`
- [x] T008 [P] Add `FieldSummary.java` in `src/main/java/com/solidanalysis/scanner/model/FieldSummary.java`
- [x] T009 [P] Add `ParameterSummary.java` in `src/main/java/com/solidanalysis/scanner/model/ParameterSummary.java`
- [x] T010 [P] Add `MethodCallSummary.java` in `src/main/java/com/solidanalysis/scanner/model/MethodCallSummary.java`
- [x] T011 [P] Add `MethodSummary.java` in `src/main/java/com/solidanalysis/scanner/model/MethodSummary.java`
- [x] T012 Add `OutputArtifactNamer.java` in `src/main/java/com/solidanalysis/scanner/OutputArtifactNamer.java` mapping scan-root-relative path to unique `output/*.json` filename per [research.md](./research.md)
- [x] T013 Add `ArtifactJsonWriter.java` in `src/main/java/com/solidanalysis/scanner/ArtifactJsonWriter.java` using Jackson UTF-8 output aligned with [contracts/ast-artifact.schema.json](./contracts/ast-artifact.schema.json)
- [x] T014 Add `JavaParserFacade.java` in `src/main/java/com/solidanalysis/scanner/JavaParserFacade.java` building `CombinedTypeSolver` (ReflectionTypeSolver + JavaParserTypeSolver on CLI root), `ParserConfiguration` with `JavaSymbolSolver`, and `parse(Path)` returning `CompilationUnit`
- [x] T015 [P] Add `OutputArtifactNamerTest.java` in `src/test/java/com/solidanalysis/scanner/OutputArtifactNamerTest.java` — incluir caso **duas `Foo.java`** em subpastas distintas sob a mesma raiz, assert **nomes de saída distintos** (FR-004)
- [x] T016 [P] Add `ArtifactJsonWriterTest.java` in `src/test/java/com/solidanalysis/scanner/ArtifactJsonWriterTest.java` — escrever `AstArtifact` mínimo para ficheiro temporário, assert conteúdo JSON não vazio e UTF-8
- [x] T017 [P] Add `JavaParserFacadeTest.java` in `src/test/java/com/solidanalysis/scanner/JavaParserFacadeTest.java` — ficheiro `.java` temporário simples, `parse(Path)`, assert `CompilationUnit` presente

**Checkpoint**: Modelo + parse com solver + testes de infra verdes.

---

## Phase 3: User Story 1 — Escanear e gerar artefato por ficheiro (P1) 🎯 MVP

**Goal**: Caminho absoluto → walk recursivo `.java` → um JSON não vazio por ficheiro em `output/` com nome de tipo identificável (mínimo). **Tratamento de zero `.java`**: scan completa com resumo coerente (edge case spec).

**Independent Test**: `@TempDir` com `.java` válidos; outro caso com **nenhum** `.java`.

### Tests for User Story 1 (required — JUnit 5)

- [x] T018 [P] [US1] Add `ProjectScannerIntegrationTest.java` in `src/test/java/com/solidanalysis/scanner/ProjectScannerIntegrationTest.java` using `@TempDir` — dois `.java` em subpastas, assert 2 ficheiros JSON únicos, tamanho > 0, e nome de classe esperado no conteúdo
- [x] T019 [US1] In `ProjectScannerIntegrationTest.java` or `EmptyProjectScanTest.java` under `src/test/java/com/solidanalysis/scanner/` — `@TempDir` **sem** ficheiros `.java`, assert execução do scan termina sem excepção e resumo com **zero sucessos** (edge spec)

### Implementation for User Story 1

- [x] T020 [US1] Add `AstExtractor.java` in `src/main/java/com/solidanalysis/scanner/AstExtractor.java` with minimal pass: preencher `sourceFile`, `primaryType.name`, `primaryType.kind`, listas vazias onde aplicável (placeholder até US2)
- [x] T021 [US1] Add `ProjectScanner.java` in `src/main/java/com/solidanalysis/scanner/ProjectScanner.java` — `Files.walk`, filtrar `*.java`, **arvore vazia de `.java` OK** (nenhum ficheiro escrito, resumo coerente), para cada ficheiro parse via `JavaParserFacade`, extrair com `AstExtractor`, nomear com `OutputArtifactNamer`, escrever com `ArtifactJsonWriter`
- [x] T022 [US1] Add `ScannerCli.java` in `src/main/java/com/solidanalysis/scanner/ScannerCli.java` with `public static void main` delegando a método testável `static int run(String[] args)` (ou equivalente) — validação per [contracts/cli.md](./contracts/cli.md); **exit codes** per matriz no contrato; invocar `ProjectScanner`
- [x] T023 [US1] Add `ScannerCliTest.java` in `src/test/java/com/solidanalysis/scanner/ScannerCliTest.java` — exercitar `run(...)` com 0 args, >1 args, caminho relativo, caminho inexistente, assert códigos de saída **≠ 0** e mensagens em **stderr** onde aplicável
- [x] T024 [US1] Smoke: document in `README.md` the exact `mvn -q package` + `java -jar target/...jar <abs>` command from [quickstart.md](./quickstart.md) and verify manually or with a thin script (shade já definido em T001)

**Checkpoint**: MVP técnico — scan + JSON mínimo + CLI testável; **FR-005 pendente até US2**.

---

## Phase 4: User Story 2 — Estrutura completa no JSON (P2)

**Goal**: FR-005 — abstrato, superclasse, interfaces, campos, métodos, parâmetros, `methodCalls` por método.

**Independent Test**: `StaticJavaParser` / facade sobre *string* de código com herança, interface, campo, método com chamada; assert no objeto ou JSON.

### Tests for User Story 2 (required)

- [x] T025 [P] [US2] Add `AstExtractorTest.java` in `src/test/java/com/solidanalysis/scanner/AstExtractorTest.java` — fonte em string: classe simples com nome correcto
- [x] T026 [P] [US2] Add cases in `src/test/java/com/solidanalysis/scanner/AstExtractorTest.java` for superclass + implemented interfaces + fields + method signatures
- [x] T027 [US2] Add case in `src/test/java/com/solidanalysis/scanner/AstExtractorTest.java` asserting `methodCalls` contém chamada esperada no método relevante

### Implementation for User Story 2

- [x] T028 [US2] Implement full `AstExtractor` traversal in `src/main/java/com/solidanalysis/scanner/AstExtractor.java` — primeira `ClassOrInterfaceDeclaration` top-level, preencher todos os campos de `TypeSummary` e `MethodSummary` conforme [data-model.md](./data-model.md)
- [x] T029 [US2] Implement method body visit for `MethodCallExpr` in `src/main/java/com/solidanalysis/scanner/AstExtractor.java` — tentar symbol resolution; preencher `MethodCallSummary.resolved` e campos opcionais ou fallback textual per [research.md](./research.md)

**Checkpoint**: Artefacto JSON alinhado ao schema para análise SOLID posterior (**FR-005** satisfeito).

---

## Phase 5: User Story 3 — Resiliência e resumo (P3)

**Goal**: FR-007/FR-008 — falha por ficheiro no stdout, continuar; linha final com contagens.

**Independent Test**: Temp dir com um `.java` inválido + válidos; assert processo completa, stdout contém path do inválido e resumo com números coerentes.

### Tests for User Story 3 (required)

- [x] T030 [US3] Add `ScannerResilienceTest.java` in `src/test/java/com/solidanalysis/scanner/ScannerResilienceTest.java` capturing `System.out` (ou injeção de `Consumer<String>` se refactor) — mistura válido/inválido, assert success/failure counts e ausência de excepção não tratada

### Implementation for User Story 3

- [x] T031 [US3] Refactor `ProjectScanner.java` in `src/main/java/com/solidanalysis/scanner/ProjectScanner.java` to catch parse/extraction failures per file, increment counters, print file path to stdout, never abort batch
- [x] T032 [US3] Add summary line at end of run in `src/main/java/com/solidanalysis/scanner/ProjectScanner.java` or `ScannerCli.java` matching [contracts/cli.md](./contracts/cli.md) (e.g. parsed/failed counts)
- [x] T033 [US3] Align `ScannerCli.java` exit codes with [contracts/cli.md](./contracts/cli.md) matrix; ensure `README.md` documents invalid-args vs partial file failures vs empty tree

**Checkpoint**: Comportamento robusto em bases reais.

---

## Phase 6: User Story 4 — Resolução de tipos no projeto (P2)

**Goal**: Tipos referenciados sob a mesma raiz reflectem solver configurado (FR-006).

**Independent Test**: Dois ficheiros `.java` no mesmo `@TempDir` referenciando um ao outro; assert tipo ou chamada resolvida quando aplicável.

### Tests for User Story 4 (required)

- [x] T034 [US4] Add `TypeResolutionIntegrationTest.java` in `src/test/java/com/solidanalysis/scanner/TypeResolutionIntegrationTest.java` — mini projeto em temp dir, assert `resolved` true ou tipo de campo/call coerente com tipos locais

### Implementation for User Story 4

- [x] T035 [US4] Review and harden `JavaParserFacade.java` in `src/main/java/com/solidanalysis/scanner/JavaParserFacade.java` — garantir `JavaParserTypeSolver` cobre raiz completa e mesma instância de solver usada em todos os parses do mesmo scan
- [x] T036 [US4] Document limitações (sem JARs classpath externos) in `README.md` at repo root

**Checkpoint**: Resolução útil para chamadas dentro do projeto fonte.

---

## Phase 7: Polish & cross-cutting

**Purpose**: Documentação final da iteração, Javadoc, validação opcional de schema.

- [x] T037 [P] Complete `ITERACAO.md` at repo root (expand stub from T005) with implemented scope, decisions, run/test for branch `001-ast-parser`
- [x] T038 [P] Finalize `README.md` at repo root with accurate `mvn package`, `java -jar …`, CWD/`output/` note from [quickstart.md](./quickstart.md)
- [x] T039 [P] Add Javadoc to all public types and public methods under `src/main/java/com/solidanalysis/scanner/`
- [x] T040 Cross-check edge cases from [spec.md](./spec.md) (non-`.java` ignored, UTF-8) — adjust code or document gaps in `ITERACAO.md`
- [ ] T041 Optional: add test validating sample JSON against `specs/001-ast-parser/contracts/ast-artifact.schema.json` using a JSON Schema validator dependency — **omitido** (ver `ITERACAO.md`)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)** → início imediato
- **Foundational (Phase 2)** → depende de Setup; **bloqueia** US1–US4; **inclui testes T015–T017**
- **US1 (Phase 3)** → após Fundação; **MVP técnico (não FR-005 completo)**
- **US2 (Phase 4)** → após US1
- **US3 (Phase 5)** → após US2 recomendado
- **US4 (Phase 6)** → pode sobrepor com US3 após US2
- **Polish (Phase 7)** → após histórias desejadas

### User Story Dependencies

- **US1**: só Fundação (+ testes de fundação)
- **US2**: US1 scaffold
- **US3**: `ProjectScanner` estável
- **US4**: `JavaParserFacade` + extração com resolução

### Parallel Opportunities

- T006–T011, T015–T017, T018, T025–T026, T037–T039 em paralelo quando dependências satisfeitas

---

## Parallel Example: Phase 2 models + tests

```bash
# After T001 complete:
Task: "Model POJOs T006–T011 in parallel"
Task: "T015 OutputArtifactNamerTest.java (after T012)"
Task: "T016 ArtifactJsonWriterTest.java (after T013)"
Task: "T017 JavaParserFacadeTest.java (after T014)"
```

---

## Implementation Strategy

### MVP First (User Story 1 only)

1. Complete Phase 1–2 (incl. **T015–T017**)  
2. Complete Phase 3 (US1) — **T001–T024**  
3. `mvn test` + smoke JAR  
4. **Nota**: só após Phase 4 é que FR-005 / SC-003 estão garantidos pelo código

### Incremental Delivery

1. Setup + Fundação + testes de fundação  
2. US1 → scan + JSON mínimo + CLI testável  
3. US2 → contrato JSON completo  
4. US3 → error handling + resumo  
5. US4 → tipos locais  
6. Polish → docs + Javadoc

---

## Summary counts

| Métrica | Valor |
|---------|------:|
| Total tasks | 41 (40 concluídas; T041 opcional omitida) |
| Phase 1 | 5 |
| Phase 2 | 12 (9 impl + 3 tests) |
| US1 | 7 |
| US2 | 5 |
| US3 | 4 |
| US4 | 3 |
| Polish | 5 |
| Tasks with [P] | 17 |

**Suggested MVP scope**: Phases 1–3 (**T001–T024**) — MVP técnico; **fecho de spec completo** requer até **T029** mínimo.  
**Independent test criteria**: copiados das secções Goal/Independent Test por fase acima.

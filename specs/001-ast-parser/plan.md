# Implementation Plan: Parser AST (etapa 1)

**Branch**: `001-ast-parser` | **Date**: 2026-03-20 | **Spec**:
[spec.md](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/spec.md)

**Input**: Feature specification from
`/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/spec.md`

## Summary

Entregar um **scanner CLI** em Java 17 (Maven) que recebe um **diretório absoluto** de um projeto
fonte, enumera recursivamente ficheiros `.java`, parseia cada um com **JavaParser** + **symbol
solver** (`CombinedTypeSolver` com raiz do projeto), extrai um **modelo de domínio** (tipo
principal, herança, interfaces, campos, métodos, chamadas por método) e grava **um JSON por
ficheiro** em `output/`, com nomes únicos por caminho relativo. Falhas por ficheiro são registadas
em stdout e a execução termina com **contagens** de sucesso e falha. Testes JUnit 5 sem rede nem
árvores externas (fonte em string + diretórios temporários quando necessário).

## Technical Context

**Language/Version**: Java 17 (source/target)  
**Primary Dependencies**: `com.github.javaparser:javaparser-core:3.26.3`,
`com.github.javaparser:javaparser-symbol-solver-core:3.26.3`, `com.fasterxml.jackson.core:jackson-databind:2.17.2`  
**Storage**: Ficheiros locais — leitura `.java` sob raiz de entrada; escrita JSON em
`{user.dir}/output/`  
**Testing**: JUnit 5 (`org.junit.jupiter:junit-jupiter:5.10.2`)  
**Target Platform**: JVM — Linux/macOS/Windows (path absoluto na CLI)  
**Project Type**: CLI / ferramenta de análise estática (fat JAR)  
**Performance Goals**: Não crítico nesta iteração; processar projetos típicos de desenvolvimento
sem objetivo numérico fixo  
**Constraints**: Sem rede em testes; UTF-8; não abortar scan por parse inválido (FR-007); parser
próprio proibido (FR-000)  
**Scale/Scope**: Etapa 1 apenas — export AST estruturado; sem regras SOLID ainda

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Gate | Status |
|------|--------|
| Branch `001-ast-parser` alinhada a `NNN-nome-curto` | OK |
| Layout Maven `src/main/java`, `src/test/java`, `pom.xml`; pacote `com.solidanalysis` + `scanner` | OK (plano) |
| Código inglês, Javadoc público, comentários onde necessário | OK (exigência de implementação) |
| JUnit 5, testes independentes, sem rede/arquivos externos fixos | OK (strings + temp dirs) |
| `ITERACAO.md` / `README` / `CHANGELOG` | README/CHANGELOG a criar na implementação; `ITERACAO.md` na branch |
| Commits em português, pequenos | OK (disciplina de PR) |

Nenhuma violação que exija Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-ast-parser/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── spec.md
├── contracts/
│   ├── ast-artifact.schema.json
│   └── cli.md
└── tasks.md              # /speckit.tasks (não criado por este comando)
```

### Source Code (repository root)

```text
solid-static-analysis/
├── pom.xml
├── README.md
├── CHANGELOG.md
├── src/main/java/com/solidanalysis/scanner/
│   ├── ScannerCli.java              # main, args, validação caminho absoluto
│   ├── ProjectScanner.java          # walk .java, orquestra parse + write
│   ├── JavaParserFacade.java        # parse + TypeSolver para uma raiz
│   ├── AstExtractor.java            # CompilationUnit → AstArtifact (DTO)
│   └── model/                       # POJOs espelhando data-model.md
└── src/test/java/com/solidanalysis/scanner/
    ├── AstExtractorTest.java
    ├── ProjectScannerIntegrationTest.java
    ├── OutputArtifactNamerTest.java
    ├── ArtifactJsonWriterTest.java
    ├── JavaParserFacadeTest.java
    ├── ScannerCliTest.java
    ├── ScannerResilienceTest.java
    ├── TypeResolutionIntegrationTest.java
    └── ...
```

**Structure Decision**: Pacote único `com.solidanalysis.scanner` conforme spec; modelo em
subpacote `model` se o número de classes crescer; testes espelhados 1:1 com classes públicas
principais.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| — | — | — |

## Phase 0: Research

**Output**: [research.md](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/research.md)

Decisões fechadas: versões Maven, `CombinedTypeSolver`, estratégia de nomes JSON, base `output/`
via `user.dir`, tratamento de chamadas não resolvidas, fat JAR.

## Phase 1: Design & contracts

**Outputs**:

- [data-model.md](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/data-model.md)
- [contracts/ast-artifact.schema.json](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/contracts/ast-artifact.schema.json)
- [contracts/cli.md](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/contracts/cli.md)
- [quickstart.md](/home/felipe/Documents/AS/solid-static-analysis/specs/001-ast-parser/quickstart.md)

## Constitution Check (post-design)

Reavaliação após artefatos de design:

- Contratos e modelo mantêm-se **agnósticos de violação** da constituição; implementação deve
  cumprir Javadoc, JUnit 5, e `output/` + pacotes conforme acima.
- **Follow-up**: adicionar `ITERACAO.md` na raiz da branch e atualizar `README.md` quando o código
  existir (Princípio V).

## Próximo passo

Executar **`/speckit.tasks`** para gerar `tasks.md` a partir deste plano e dos contratos.

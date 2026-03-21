# Implementation Plan: Geração de grafos DOT

**Branch**: `002-generate-dot-graphs` | **Date**: 2026-03-20 | **Spec**: [spec.md](./spec.md)  
**Input**: Feature specification from `/home/felipe/Documents/AS/solid-static-analysis/specs/002-generate-dot-graphs/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

A Etapa 2 lê todos os `*.json` de um diretório de saída da Etapa 1, reconstrói o modelo AST em memória e gera sete famílias de grafos em DOT sob `graphs/`, incluindo CFGs por método em `g7_cfg/`. A abordagem técnica: Jackson para desserialização, componente único de filtragem de tipos (primitivos, `java.*`/`javax.*`, `System.out`), uma classe geradora por grafo no pacote `com.solidanalysis.graphs`, serialização DOT por templates de texto, e CLI unificada no JAR com flag `--graphs`. Detalhes de decisão em [research.md](./research.md); modelo lógico em [data-model.md](./data-model.md); contratos em [contracts/](./contracts/).

## Technical Context

**Language/Version**: Java 17+  
**Primary Dependencies**: Jackson Databind (existente), JavaParser apenas indireto (Etapa 1); Etapa 2 não exige novas dependências obrigatórias para DOT  
**Storage**: Ficheiros JSON de entrada (artefatos Etapa 1); ficheiros `.dot` de saída em `graphs/`  
**Testing**: JUnit 5; testes apenas com objetos construídos em memória (sem I/O de fixtures em disco)  
**Target Platform**: JVM — Linux/macOS/Windows (paths absolutos na CLI)  
**Project Type**: CLI / biblioteca empacotada como JAR sombreado  
**Performance Goals**: Adequado a projetos pequenos/médios (ordem de centenas de ficheiros JSON); sem requisito numérico explícito na spec  
**Constraints**: Determinismo da saída DOT; não alterar pacote `com.solidanalysis.scanner` exceto ponto de entrada / wiring CLI se necessário  
**Scale/Scope**: Sete tipos de grafo + leitor JSON + testes unitários por regra crítica  

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

**Pre-Phase 0**: PASS (branch `002-generate-dot-graphs`, escopo isolado).  
**Post-Phase 1**: PASS — design prevê `com.solidanalysis.graphs`, JUnit 5 sem ficheiros externos, Javadoc; `ITERACAO.md` / `README` ficam para a fase de implementação (tarefas).

**Pós-análise (tasks)**: testes dedicados para DTOs, `ProjectJsonLoader`, `ParsedProject`, `DotWriter`,
`G6InterfaceUsageGraphGenerator`, casos de diretório vazio e G5 só com interfaces; `CHANGELOG.md` na
Phase 6; filtros FR-010 também em G2/G5 (ver `tasks.md` T028).

## Project Structure

### Documentation (this feature)

```text
specs/002-generate-dot-graphs/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   ├── cli-graph-generation.md
│   └── dot-graph-output.md
└── tasks.md             # Phase 2 output (/speckit.tasks — NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/solidanalysis/
├── scanner/                 # Etapa 1 — não alterar lógica de parsing salvo integração CLI
└── graphs/                  # Etapa 2 — leitor JSON, filtros, geradores G1–G7, escrita DOT, orquestração

src/test/java/com/solidanalysis/
└── graphs/                  # Testes unitários (memória apenas)
```

**Structure Decision**: Projeto Maven único; novo pacote `graphs` espelhado em testes, sem submódulos.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Nenhuma violação identificada.

## Phase 0 & 1 — Outputs

| Artefato | Path |
|----------|------|
| Research | [research.md](./research.md) |
| Data model | [data-model.md](./data-model.md) |
| CLI contract | [contracts/cli-graph-generation.md](./contracts/cli-graph-generation.md) |
| DOT conventions | [contracts/dot-graph-output.md](./contracts/dot-graph-output.md) |
| Quickstart | [quickstart.md](./quickstart.md) |

## Next step

Gerar lista executável com `/speckit.tasks` → `tasks.md` (Fase 2 de execução).

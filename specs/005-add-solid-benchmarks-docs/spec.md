# Feature Specification: Benchmarks e Documentos Human-Readable de Scoring

**Feature Branch**: `005-add-solid-benchmarks-docs`  
**Created**: 2026-03-21  
**Status**: Draft  
**Input**: User description: ETAPA 5 — Benchmarks SOLID e documento human-readable do scoring por classe

**Constitution**: Feature branch naming, testing, and layout MUST align with
`.specify/memory/constitution.md`.

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Gerar documentos human-readable do scoring por classe (Priority: P1)

Como analista ou revisor de arquitetura, eu executo `--score` em um projeto e recebo arquivos `.txt` fáceis de ler com o resultado do scoring por classe e por princípio (O/S/L/I/D), além de um resumo do projeto com distribuição e ranking.

**Why this priority**: Isso transforma métricas técnicas em um documento pronto para discussão e priorização de correções.

**Independent Test**: Pode ser validado executando `--score` sobre um diretório de saída que já contenha artefatos de scoring e verificando a geração de `output/<path-sanitizado>/results/`.

**Acceptance Scenarios**:

1. **Given** um projeto com arquivos de scoring por classe em `scoring/`, **When** o pipeline for executado em modo `--score`, **Then** `results/<Classe>.txt` é gerado para cada classe presente em `scoring/`.
2. **Given** um arquivo `results/<Classe>.txt` gerado, **When** eu verificar cabeçalho e seções, **Then** o cabeçalho contém `CLASSE`, `PROJETO`, `SCORE GERAL`, `ESTRATÉGIA` e cada princípio (O/S/L/I/D) aparece com o símbolo correto (`[!]`, `[~]`, `[ok]`) e a linha de conteúdo esperada.

---

### User Story 2 - Disponibilizar benchmarks para validação do scorer (Priority: P2)

Como usuário que valida o scorer, eu preciso de projetos de benchmark que simulem um supermercado com violações SOLID (bad-project) e um supermercado seguindo SOLID (good-project), e um conjunto de casos de referência segmentados por princípio para comparação.

**Why this priority**: Benchmarks permitem validar se o scorer identifica mais riscos no caso ruim do que no caso bom.

**Independent Test**: Pode ser validado verificando a existência das pastas e conferindo que as subpastas por princípio (`*_bad`/`*_good`) existem e que `bad-project/` e `good-project/` contêm as entidades do domínio com organização e marcações esperadas.

**Acceptance Scenarios**:

1. **Given** o repositório com a estrutura `benchmarks/`, **When** eu inspeciono as pastas `bad-project/`, `good-project/` e os pares por princípio (`*_bad`/`*_good`), **Then** a estrutura segue as regras descritas e as violações estão documentadas nos pontos problemáticos do `bad-project/`.

---

### User Story 3 - Gerar resumo do projeto com ranking (Priority: P3)

Como analista, eu preciso de um `project_summary.txt` contendo distribuição dos riscos por princípio e um ranking de classes do pior para o melhor, para priorizar correções rapidamente.

**Why this priority**: O ranking consolida o resultado do scorer em uma visão única e comparável entre projetos e casos de benchmark.

**Independent Test**: Pode ser validado gerando `results/project_summary.txt` a partir dos artefatos em `scoring/` e comparando a ordem do ranking com o `ranking` produzido pelo scorer.

**Acceptance Scenarios**:

1. **Given** `scoring/project_summary.json` com o array `ranking`, **When** eu gerar `results/project_summary.txt`, **Then** as entradas do ranking aparecem na mesma ordem (pior → melhor) e com os mesmos níveis de risco e princípio mais violado por classe.

---

### Edge Cases

- Quando `results/` já existe para o mesmo projeto, o pipeline deve substituir (ou atualizar) os arquivos relevantes para evitar arquivos obsoletos.
- Quando não há arquivos de scoring por classe no diretório `scoring/`, o pipeline deve falhar de forma controlada (sem gerar documentos parciais) e comunicar claramente a causa.
- Quando o projeto tem apenas 1 classe, os documentos devem ainda ser gerados com cabeçalho, seções e ranking corretos (sem supor um número mínimo de classes).

## Requirements *(mandatory)*

### Functional Requirements
- **FR-001**: O projeto MUST adicionar a pasta `benchmarks/` com a estrutura paralela descrita:
  - `benchmarks/<principio>_bad` e `benchmarks/<principio>_good`: casos de referência por princípio, com fontes Java versionadas para validação local/offline.
  - `benchmarks/bad-project/`: contém um supermercado com violações SOLID documentadas com comentários contendo o marcador `SOLID-VIOLATION: <principio>` imediatamente acima dos trechos problemáticos.
  - `benchmarks/good-project/`: contém um supermercado equivalente, organizado por responsabilidades, com ausência das violações documentadas no `bad-project/`.
- **FR-002**: Os dois projetos do supermercado MUST conter as entidades do domínio listadas na descrição do usuário (`Product`, `Category`, `Customer`, `Cart`, `CartItem`, `Order`, `Payment`, `Receipt`, `Inventory`, `Discount`, `Cashier`, `Register`) e permitir que o scorer opere sobre elas durante a validação.
- **FR-003**: Quando `--score` for executado em um diretório de saída que já contém artefatos de scoring, o pipeline MUST também gerar documentos human-readable em `output/<path-sanitizado>/results/`.
- **FR-004**: Para cada classe presente em `scoring/<Classe>.json`, o pipeline MUST gerar `results/<Classe>.txt` com o cabeçalho e seções no formato solicitado, incluindo `CLASSE`, `PROJETO`, `SCORE GERAL`, `ESTRATÉGIA` (com fator de relaxamento quando aplicável), as seções O/S/L/I/D e a seção `Legenda de símbolos:` com o mapeamento `ALTO`/`MEDIO`/`BAIXO` para `[!]`/`[~]`/`[ok]`.
- **FR-005**: O mapeamento de risco MUST ser consistente:
  - `ALTO` → `[!]`
  - `MEDIO` → `[~]`
  - `BAIXO` → `[ok]`
- **FR-006**: Cada seção de princípio MUST exibir o risco do princípio e, quando existirem detalhes/indicadores no scoring por classe, MUST incluir linhas com as sugestões/descrições correspondentes; caso não existam indicadores para o princípio, a seção deve permanecer presente com um conteúdo consistente e sem quebrar o formato.
- **FR-007**: O pipeline MUST gerar `results/project_summary.txt` contendo:
  - `RESUMO DO PROJETO`, `ESTRATÉGIA DE CLASSIFICAÇÃO` e `PRINCÍPIO MAIS VIOLADO`
  - `DISTRIBUIÇÃO POR PRINCÍPIO` com contagens por risco (ALTO/MEDIO/BAIXO)
  - `RANKING DE CLASSES (pior para melhor)` espelhando o ordering do ranking produzido em `scoring/project_summary.json`.
- **FR-008**: O resultado human-readable MUST ser produzido no mesmo fluxo do `--score`, garantindo que a execução do pipeline gere tanto `scoring/` quanto `results/` sem passos manuais adicionais.

### Key Entities *(include if feature involves data)*
- **Projetos de benchmark**: `bad-project/`, `good-project/` e casos segmentados por princípio (`*_bad`/`*_good`) usados para validação do comportamento do scorer.
- **Artefatos de scoring**: arquivos de entrada em `scoring/` (por classe e um resumo do projeto) que contêm níveis de risco e detalhes por princípio.
- **Documentos human-readable**: arquivos `.txt` em `results/` (um por classe e um `project_summary.txt`) que traduzem os artefatos de scoring para leitura humana.

### Assumptions and Dependencies

- O pipeline de `--score` já gera (ou pressupõe a existência de) `graphs/`, `algorithms/` e arquivos JSON em `scoring/` (incluindo `scoring/project_summary.json`).
- O gerador de documentos human-readable deve usar o conteúdo desses JSONs como fonte de verdade para níveis de risco, detalhes por princípio e ranking.

## Success Criteria *(mandatory)*

### Measurable Outcomes
- **SC-001**: Para um projeto com `N` classes no `scoring/`, após executar `--score` devem existir exatamente `N` arquivos `results/<Classe>.txt` e 1 arquivo `results/project_summary.txt` (onde `N` corresponde ao número de arquivos `scoring/<Classe>.json`).
- **SC-002**: Os testes automatizados devem validar que 100% dos arquivos `results/<Classe>.txt` seguem o padrão de cabeçalho e incluem a seção `Legenda de símbolos:` com o mapeamento `ALTO`/`MEDIO`/`BAIXO` para `[!]`/`[~]`/`[ok]`, além de usar os símbolos corretos em cada princípio.
- **SC-003**: Os testes automatizados devem validar que o ranking em `results/project_summary.txt` está na mesma ordem (pior → melhor) e com os mesmos níveis de risco/princípio de referência que o `ranking` em `scoring/project_summary.json`.
- **SC-004**: Em um smoke test end-to-end, executar `--score` em `benchmarks/bad-project/` deve produzir mais marcações de `ALTO` nos princípios que estão documentados como violados, do que executar `--score` em `benchmarks/good-project/`.

# Research: ETAPA 5 — Benchmarks SOLID e Documentos Human-Readable do Scoring

## Decision 1: Gerar `.txt` exclusivamente a partir de `scoring/*.json`

- **Decision**: O gerador de documentos human-readable lerá `scoring/<Classe>.json` (por classe) e `scoring/project_summary.json` (visão geral) e produzirá `results/<Classe>.txt` e `results/project_summary.txt`.
- **Rationale**: Mantém a Etapa 5 desacoplada de etapas anteriores (scanner/graphs/algorithms). O output textual será uma projeção determinística dos artefatos já validados.
- **Alternatives considered**: Reexecutar análise/algoritmos dentro da Etapa 5 para “recalcular” os indicadores. Isso duplicaria lógica e criaria divergência entre JSON e texto.

## Decision 2: Determinismo de ordenação e ranking

- **Decision**: O ranking do `project_summary.txt` e a ordenação das classes devem seguir exatamente o array `ranking` do `scoring/project_summary.json`.
- **Rationale**: O scorer já define uma regra determinística de desempate (por nome da classe). Reordenar manualmente no writer poderia introduzir inconsistência.
- **Alternatives considered**: Ordenar “na mão” usando `overall` e `worst`. Isso replicaria regras e poderia divergir do ranking do JSON.

## Decision 3: Estratégia de classificação e fator de relaxamento no cabeçalho

- **Decision**: A linha `ESTRATÉGIA` do relatório por classe será construída a partir de:
  - `classificationStrategy` do `scoring/<Classe>.json`
  - `relaxFactor` do mesmo JSON
- **Rationale**: O spec exige exibição do fator de relaxamento “quando aplicável”. Esse fator já está disponível no output do scorer.
- **Alternatives considered**: Calcular o relaxFactor novamente no writer. Isso tornaria o output dependente de regras internas e poderia divergir do JSON.

## Decision 4: Mapeamento de risco para símbolos e seções por princípio

- **Decision**: Para cada princípio (O/S/L/I/D), a seção mostrará o nível do princípio (ScoreLevel) e em seguida listará as descrições presentes em `scores[<principle>].indicators[].detail`.
- **Rationale**: O fixture de scoring já contém textos detalhados por indicador; isso garante que o documento human-readable “explica” o risco com conteúdo consistente.
- **Alternatives considered**: Listar apenas um indicador “mais forte” ou resumir indicadores. Isso removeria informação útil e faria o documento menos testável.

## Decision 5: Formato estável e tolerante a ausência de indicadores

- **Decision**: Mesmo que `indicators` esteja vazio para um princípio, o writer manterá a seção presente e exibirá um conteúdo consistente (ex.: sem risco identificado).
- **Rationale**: O spec exige que o formato não quebre e que as seções O/S/L/I/D existam sempre.
- **Alternatives considered**: Condicionar a seção à existência de indicadores. Isso alteraria o layout e criaria falsos negativos nos testes de formatação.

## Decision 6: Benchmarks como artefatos de dados

- **Decision**: A estrutura `benchmarks/` será adicionada ao repo como código-fonte de referência versionado, preservando a estrutura interna dos subdiretórios que representam princípios.
- **Rationale**: A Etapa 5 é a etapa final e o objetivo inclui validação por comparação “bad vs good”; benchmarks versionados garantem reprodutibilidade.
- **Alternatives considered**: Baixar o repositório de benchmarks em runtime (por exemplo, via script). Isso tornaria a execução frágil (rede) e menos auditável.


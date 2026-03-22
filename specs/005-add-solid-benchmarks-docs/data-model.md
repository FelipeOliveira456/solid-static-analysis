# Data Model: ETAPA 5 — Documentos Human-Readable de Scoring

## Overview

Esta etapa transforma artefatos JSON do scorer (Etapa 4) em documentos textuais human-readable:

- `output/<path-sanitizado>/results/<Classe>.txt`
- `output/<path-sanitizado>/results/project_summary.txt`

Os arquivos são gerados no mesmo fluxo de execução do comando `--score`.

## Inputs (fontes de verdade)

### Per-class scoring JSON

Arquivo: `scoring/<Classe>.json`

- `class` (string): nome da classe
- `overall` (string): um de `ALTO | MEDIO | BAIXO`
- `classificationStrategy` (string): uma de `FIXED_THRESHOLD | FIXED_THRESHOLD_RELAXED | Z_SCORE`
- `relaxFactor` (number): fator `f(n)=n/(n+k)` quando aplicável
- `projectPath` (string): caminho absoluto do projeto
- `scores` (object): chaves `S | O | L | I | D`, cada uma com:
  - `score` (string): `ALTO | MEDIO | BAIXO`
  - `indicators` (array): lista de indicadores com `templateId`, `value` e `detail`

### Project summary JSON

Arquivo: `scoring/project_summary.json`

- `projectPath` (string)
- `classificationStrategy` (string)
- `relaxFactor` (number)
- `mostViolatedPrinciple` (string ou null): letra `S | O | L | I | D`
- `principleDistribution` (object): contagens por princípio e risco
- `ranking` (array): lista de entradas com `class`, `overall`, `worst`

## Output (documentos `.txt`)

### Report por classe: `<Classe>.txt`

Estrutura (campos obrigatórios no cabeçalho):

- Linha `================================================================================`
- `CLASSE: <class>`
- `PROJETO: <projectPath>`
- `SCORE GERAL: <overall>`
- `ESTRATÉGIA: <classificationStrategy> ... (fator de relaxamento quando aplicável)`
- Linha `================================================================================`

Estrutura de seções (sempre em ordem fixa):

- `O — Open/Closed: <risk-level>`
- `S — Single Responsibility: <risk-level>`
- `L — Liskov Substitution: <risk-level>`
- `I — Interface Segregation: <risk-level>`
- `D — Dependency Inversion: <risk-level>`

Em cada seção, após a linha de princípio, o writer lista:

- Para cada item em `indicators` do princípio: uma linha com símbolo e `detail`

Símbolos:

- `ALTO` → `[!]`
- `MEDIO` → `[~]`
- `BAIXO` → `[ok]`

Quando `indicators` estiver vazio:

- mantém a seção e exibe conteúdo consistente (sem listar linhas de indicadores).

### Project summary: `project_summary.txt`

Estrutura:

- Cabeçalho com:
  - `RESUMO DO PROJETO: <projectPath>`
  - `ESTRATÉGIA DE CLASSIFICAÇÃO: <classificationStrategy>`
  - `PRINCÍPIO MAIS VIOLADO: <letra + nome>` ou placeholder quando null
- Seção `DISTRIBUIÇÃO POR PRINCÍPIO` com contagens:
  - `S — Single Responsibility:  ALTO: x  MEDIO: y  BAIXO: z`
  - `O — Open/Closed:            ...`
  - `L — Liskov Substitution:    ...`
  - `I — Interface Segregation: ...`
  - `D — Dependency Inversion:   ...`
- Seção `RANKING DE CLASSES (pior para melhor)` com uma linha por entrada do array `ranking`,
  preservando a ordem.

## Formatting Rules (estabilidade de testes)

- Ordens fixas de seções e distribuição.
- Ranking segue `ranking[]` sem reordenação.
- Mapeamento de letras para nomes de princípios:
  - `S` → `Single Responsibility`
  - `O` → `Open/Closed`
  - `L` → `Liskov Substitution`
  - `I` → `Interface Segregation`
  - `D` → `Dependency Inversion`


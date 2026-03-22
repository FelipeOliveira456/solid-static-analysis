# Contract: Arquivos Human-Readable de Scoring (Etapa 5)

## Escopo

Este contrato define o formato esperado dos arquivos `.txt` gerados pela Etapa 5 a partir de `scoring/*.json`:

- `results/<Classe>.txt`
- `results/project_summary.txt`

## Report por classe: `results/<Classe>.txt`

### Cabeçalho (obrigatório)

Deve conter, nesta ordem:

1. Linha de separador: `================================================================================`
2. `CLASSE: <Classe>`
3. `PROJETO: <projectPath>`
4. `SCORE GERAL: <ALTO|MEDIO|BAIXO>`
5. `ESTRATÉGIA: <FIXED_THRESHOLD|FIXED_THRESHOLD_RELAXED|Z_SCORE> ...`
6. Linha de separador: `================================================================================`

### Seções por princípio (obrigatórias)

Deve conter cinco blocos, nesta ordem fixa:

- `O — Open/Closed: <ALTO|MEDIO|BAIXO>`
- `S — Single Responsibility: <ALTO|MEDIO|BAIXO>`
- `L — Liskov Substitution: <ALTO|MEDIO|BAIXO>`
- `I — Interface Segregation: <ALTO|MEDIO|BAIXO>`
- `D — Dependency Inversion: <ALTO|MEDIO|BAIXO>`

### Linhas de indicadores

Para cada indicador em `scores[<principle>].indicators[]`:

- prefixar com símbolo do risco do princípio:
  - `ALTO` → `[!]`
  - `MEDIO` → `[~]`
  - `BAIXO` → `[ok]`
- em seguida inserir `detail` do indicador.

### Legenda de símbolos (obrigatória)

Deve conter, após as seções de princípios:

- Linha `Legenda de símbolos:`
- Linhas de mapeamento contendo:
  - `[!]` associado a `ALTO`
  - `[~]` associado a `MEDIO`
  - `[ok]` associado a `BAIXO`

## Project summary: `results/project_summary.txt`

### Cabeçalho (obrigatório)

Deve conter:

- `RESUMO DO PROJETO: <projectPath>`
- `ESTRATÉGIA DE CLASSIFICAÇÃO: <classificationStrategy>`
- `PRINCÍPIO MAIS VIOLADO: <letra + nome>` ou placeholder quando `mostViolatedPrinciple` for null

### Distribuição por princípio

Deve conter as linhas:

- `S — Single Responsibility:  ALTO: x  MEDIO: y  BAIXO: z`
- `O — Open/Closed:            ALTO: x  MEDIO: y  BAIXO: z`
- `L — Liskov Substitution:    ALTO: x  MEDIO: y  BAIXO: z`
- `I — Interface Segregation:  ALTO: x  MEDIO: y  BAIXO: z`
- `D — Dependency Inversion:   ALTO: x  MEDIO: y  BAIXO: z`

### Ranking (obrigatório)

Deve conter o título `RANKING DE CLASSES (pior para melhor)` e uma linha por entrada do array `ranking` em `scoring/project_summary.json`, preservando a ordem.


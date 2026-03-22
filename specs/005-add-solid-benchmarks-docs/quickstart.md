# Quickstart: ETAPA 5 — Benchmarks e Documentos Human-Readable

## Objetivo

Validar o scorer de SOLID com projetos de benchmark e obter relatórios human-readable em formato `.txt`.

## Pré-requisitos

- Java 17
- Maven
- A etapa anterior deve existir no repositório (Stage 4) para que o pipeline `--score` produza `scoring/`.

## Build

```bash
mvn clean package
```

## Gerar resultados a partir de uma saída existente (`--score`)

O comando `--score` gera `scoring/` JSON e também `results/` human-readable:

```bash
java -jar target/solid-static-analysis.jar --score /absolute/path/to/project-output
```

O caminho de saída segue:

```text
/absolute/path/to/project-output/scoring/
/absolute/path/to/project-output/results/
```

## Validar benchmarks “bad vs good”

Os benchmarks são versionados em:

- `benchmarks/bad-project/`
- `benchmarks/good-project/`
- `benchmarks/bad-good-cases/` (referência)

Fluxo esperado para validar a diferença:

1. Executar o pipeline anterior (`--all`) para gerar `output/<path-sanitizado>/scoring/` para cada benchmark.
2. Executar `--score` para gerar `output/<path-sanitizado>/results/`.
3. Comparar a presença de riscos `ALTO` entre os relatórios dos princípios documentados como violados.

## Como verificar rapidamente

1. Confirmar existência de `results/<Classe>.txt` para cada classe presente em `scoring/`.
2. Confirmar existência de `results/project_summary.txt`.
3. Conferir ranking do `project_summary.txt` seguindo a ordem do `scoring/project_summary.json`.


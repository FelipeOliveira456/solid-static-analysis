# Changelog

Todas as alterações notáveis serão documentadas aqui ao integrar cada iteração em `main`.

## [Unreleased]

### Added — branch `002-generate-dot-graphs`

- Módulo `com.solidanalysis.graphs`: leitura dos JSON da Etapa 1, filtros (JDK / primitivos /
  `System.out`), geradores G1–G7 e escrita de ficheiros `.dot` em `graphs/` e `graphs/g7_cfg/`.
- CLI unificada `com.solidanalysis.SolidAnalysisCli` com modo `--graphs <ABS_DIR_JSON>`.
- Testes JUnit 5 no pacote `com.solidanalysis.graphs` (dados sintéticos; `@TempDir` só onde necessário).

### Added — branch `001-ast-parser`

- Projeto Maven `com.solidanalysis:solid-static-analysis` com JavaParser + symbol solver e export JSON.
- CLI `com.solidanalysis.scanner.ScannerCli` e scanner recursivo de ficheiros `.java`.
- Testes JUnit 5 para infraestrutura, extração, CLI e resiliência.

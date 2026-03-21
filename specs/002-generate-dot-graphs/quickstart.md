# Quickstart: Etapa 2 — grafos DOT

## Pré-requisitos

- JDK 17+
- Maven 3.8+
- (Opcional para visualização) [Graphviz](https://graphviz.org/) — comando `dot`

## Build

```bash
cd solid-static-analysis
mvn -q package
```

O JAR sombreado fica em `target/solid-static-analysis.jar`.

## 1) Gerar JSON (Etapa 1)

```bash
java -jar target/solid-static-analysis.jar /abs/path/to/java/project
```

Os artefatos aparecem em `output/<nome>/` conforme implementação atual do scanner.

## 2) Gerar grafos (Etapa 2)

Apontar para o diretório que contém os `*.json` (não para a raiz do projeto fonte):

```bash
java -jar target/solid-static-analysis.jar --graphs /abs/path/to/solid-static-analysis/output/meu-projeto
```

Saída esperada: `.../output/meu-projeto/graphs/*.dot` e `.../graphs/g7_cfg/*.dot`.

## Visualizar

```bash
dot -Tpng graphs/g1_dependency.dot -o g1.png
```

## Testes

```bash
mvn -q test
```

Testes do pacote `com.solidanalysis.graphs` usam apenas dados em memória (sem JSON em disco).

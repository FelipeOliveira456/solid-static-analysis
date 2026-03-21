# solid-static-analysis

Ferramenta de análise estática em Java: percorre um projeto fonte, parseia cada `.java` com
[JavaParser](https://github.com/javaparser/javaparser) e grava um JSON por ficheiro em `output/`
(métodos, chamadas, estruturas de fluxo como `if` / `while` / `try`, etc.). Com **`--graphs`**, lê
esses JSON e gera grafos [Graphviz DOT](https://graphviz.org/) em `output/<projeto>/graphs/`.

## Requisitos

- **JDK 17+** (o `pom.xml` fixa `maven.compiler.source` / `target` em 17)
- **Maven 3.9+**

## Como compilar

Na raiz do repositório:

```bash
mvn clean package
```

O JAR executável (shade) fica em:

```text
target/solid-static-analysis.jar
```

## Ponto de entrada do JAR

O `Main-Class` do shade é `com.solidanalysis.SolidAnalysisCli`:

- **Scan (Etapa 1)**: um argumento — raiz **absoluta** do projeto Java a analisar.
- **Grafos (Etapa 2)**: `--graphs` e o caminho **absoluto** do diretório que já contém os `*.json`
  (por exemplo `.../output/nome-do-projeto`).

## Como executar o scanner (Etapa 1)

1. Compila com o comando acima (o JAR só existe após `package`).
2. Corre o JAR com **um argumento**: caminho **absoluto** para a raiz do projeto Java a analisar.
3. Os JSON são escritos em **`./output/`** relativo ao diretório onde corres o comando (`user.dir`), não relativos ao projeto analisado.

Exemplo (ajusta os caminhos):

```bash
cd /caminho/para/solid-static-analysis
mvn -q clean package
java -jar target/solid-static-analysis.jar /caminho/absoluto/para/o/projeto-java
```

Saída típica no fim: `Parsed: N, Failed: M` em stdout. O diretório `output/` passa a conter um `.json` por ficheiro `.java` (nomes derivados do caminho para evitar colisões).

## Como gerar grafos DOT (Etapa 2)

Aponta para o diretório onde a Etapa 1 deixou os JSON (não para a raiz do projeto fonte):

```bash
java -jar target/solid-static-analysis.jar --graphs /caminho/absoluto/para/solid-static-analysis/output/meu-projeto
```

São criados `g1_dependency.dot` … `g6_interface_usage.dot` e `g7_cfg/*.dot` dentro de
`.../output/meu-projeto/graphs/`. Pré-visualização, com [Graphviz](https://graphviz.org/) instalado:

```bash
dot -Tpng graphs/g1_dependency.dot -o g1.png
```

## Como correr os testes

```bash
mvn test
```

Fontes de exemplo para integração ficam em `src/test/resources/java-fixtures/`. Ao correr os testes, o scan dessas fontes gera JSON em **`src/test/resources/java-fixtures/output/`** (para inspecionar o artefacto sem apontar o JAR a pastas externas).

## Argumentos e códigos de saída

| Situação | Exit code | Onde mensagens |
|----------|-----------|----------------|
| Scan terminado (com ou sem falhas parciais por ficheiro) | `0` | Falhas por ficheiro em **stdout**; resumo `Parsed: N, Failed: M` |
| Argumentos inválidos, caminho não absoluto, diretório inexistente, erro fatal de I/O | `≠ 0` (hoje `1`) | **stderr** |

Falhas de parse por ficheiro **não** abortam o lote. Detalhe em [specs/001-ast-parser/contracts/cli.md](specs/001-ast-parser/contracts/cli.md).

## Limitações

- Resolução de tipos via `CombinedTypeSolver` com fontes sob a raiz indicada e JDK no classpath;
  dependências Maven externas **não** são resolvidas automaticamente nesta versão.
- Por ficheiro: exporta o **primeiro** tipo `class` / `interface` top-level.

## Documentação das features

- **001 — AST / JSON**: [specs/001-ast-parser/spec.md](specs/001-ast-parser/spec.md),
  [plan.md](specs/001-ast-parser/plan.md), [tasks.md](specs/001-ast-parser/tasks.md)
- **002 — Grafos DOT**: [specs/002-generate-dot-graphs/spec.md](specs/002-generate-dot-graphs/spec.md),
  [quickstart.md](specs/002-generate-dot-graphs/quickstart.md)

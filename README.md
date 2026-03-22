# solid-static-analysis

Ferramenta de análise estática em Java: percorre um projeto fonte, parseia cada `.java` com
[JavaParser](https://github.com/javaparser/javaparser) e grava um JSON por ficheiro em `output/`
(métodos, chamadas, estruturas de fluxo como `if` / `while` / `try`, etc.). Com **`--graphs`**, lê
esses JSON e gera grafos [Graphviz DOT](https://graphviz.org/) em `output/<projeto>/graphs/`.
Com **`--analyze`**, lê os `.dot` dessa pasta e grava métricas e algoritmos em
`output/<projeto>/algorithms/` (SCC, graus, centralidade, longest path em DAG onde aplicável, LCOM,
componentes conectados, clusterização **Louvain** opcional com `--clustering`). Com **`--score`**,
lê JSON + `graphs/` + `algorithms/` e grava pontuações SOLID em `scoring/`. Com **`--all`**, corre
em sequência: scan → grafos → análise → scoring, gravando tudo em
`output/<segmento-sanitizado-da-raiz-absoluta>/` (relativo ao `user.dir`).

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
- **Algoritmos (Etapa 3)**: `--analyze` e o caminho **absoluto** do mesmo diretório de saída do
  projeto (`.../output/nome-do-projeto`), que deve conter `graphs/` com os `.dot`. Opcional:
  `--clustering` para preencher o campo `clusters` (Louvain) em G1, G3 e G4 projeção; sem a flag,
  `clusters` fica `[]` nesses JSON.
- **Scoring (Etapa 4)**: `--score` e o caminho **absoluto** do diretório que já contém os JSON na
  raiz, `graphs/` e `algorithms/`. Gera `scoring/<Classe>.json` e `scoring/project_summary.json`.
- **Pipeline completo**: `--all` e a raiz **absoluta** do projeto fonte; escreve na pasta
  `output/_<caminho_sanitizado>/` (ver `ProjectOutputPathResolver`) sob o diretório de trabalho.

### Onde a saída é gravada (utilizador vs testes)

| Fluxo | O que grava | Onde fica |
|-------|-------------|-----------|
| **Uso normal** — `java -jar … /abs/projeto` (scan) | JSON por `.java` | **`output/` no `user.dir`** (onde corres o comando); ficheiros na raiz desse `output/` (sem subpasta por projeto) |
| **`--all /abs/projeto`** | Etapas 1–4 em sequência | **`output/_caminho_sanitizado/`** no `user.dir` (subpasta derivada da raiz absoluta) |
| Depois `--graphs /abs/.../output/meu-projeto` | DOT G1–G7 | `output/meu-projeto/graphs/` |
| Depois `--analyze /abs/.../output/meu-projeto` | JSON de algoritmos | `output/meu-projeto/algorithms/` |
| **Fixtures de teste** — fontes em `src/test/resources/java-fixtures/*.java` | `mvn test` materializa Etapas 1–3 em `target/java-fixtures-pipeline-cache/` | **`src/test/resources/java-fixtures/output/`** pode incluir um snapshot de referência (scan, `graphs/`, `algorithms/`, `scoring/`); regenera com `--all` + `--output` + `--clustering` (ver abaixo) |

Ou seja: o **`output/` “de fora”** é o da raiz do projeto **solid-static-analysis** quando corres o scan
a partir daí (subpastas `output/__…/` geradas pelo `--all` sem `--output` são locais — ver `.gitignore`). O snapshot dos **fixtures** podes atualizar quando mudares os `.java` de exemplo.

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

## Como analisar grafos e gerar JSON de algoritmos (Etapa 3)

Pré-requisito: já existir `graphs/` com os `.dot` (Etapa 2) dentro do diretório de saída do projeto.

```bash
# Métricas rápidas; campo "clusters" vazio nos JSON onde o clustering é opcional
java -jar target/solid-static-analysis.jar --analyze /caminho/absoluto/para/output/meu-projeto

# Inclui Louvain (G1, G3 por classe, G4 projeção por classe)
java -jar target/solid-static-analysis.jar --analyze /caminho/absoluto/para/output/meu-projeto --clustering
```

Estrutura típica gerada em `.../output/meu-projeto/algorithms/`: `g1_algorithms.json`, `g2_…`,
`g3_algorithms/<Classe>.json`, `g4_field_algorithms/…`, `g4_projection_algorithms/…`, `g5_…`, `g6_…`,
`g7_algorithms/<Classe>_<Metodo>.json`.

## Como calcular pontuações SOLID (Etapa 4)

Pré-requisito: o diretório de saída do projeto já com JSON na raiz, `graphs/` e `algorithms/`.

```bash
java -jar target/solid-static-analysis.jar --score /caminho/absoluto/para/output/meu-projeto
```

Saída: `.../output/meu-projeto/scoring/*.json` e `project_summary.json`.

Projetos com **menos de 10 classes** usam `FIXED_THRESHOLD_RELAXED` nos JSON (com `relaxFactor`
`f(n)=n/(n+k)`). Os thresholds vêm do **`analysis.properties`** empacotado no JAR (cópia da raiz do repo). Ao correres
o CLI na raiz do **solid-static-analysis**, um **`analysis.properties` local** sobrescreve só as
chaves que definires (merge). Inclui `scoring.relax.k` e todos os `threshold.*`. Projetos com ≥10
classes usam `Z_SCORE` nos mesmos sinais contínuos.

### Pipeline completo (`--all`)

A partir do diretório onde queres criar `output/` (normalmente a raiz deste repositório):

```bash
cd /caminho/para/solid-static-analysis
java -jar target/solid-static-analysis.jar --all /caminho/absoluto/para/o/projeto-java
# opcional: escolher diretório de saída explicitamente
java -jar target/solid-static-analysis.jar --all /caminho/absoluto/para/o/projeto-java --output /caminho/absoluto/saida
```

Sem `--output`, grava em `./output/<segmento derivado do caminho absoluto>/` o scan, grafos,
algoritmos e `scoring/`. Com `--output`, grava diretamente no diretório informado. Acrescenta
`--clustering` ao `--all` para Louvain nos JSON de G1, G3 e G4 projeção (como em `--analyze
... --clustering`); sem essa flag, o passo de análise corre só com métricas (campo `clusters`
vazio onde aplicável).

### Regenerar artefactos dos fixtures (opcional)

Pipeline completo (Etapas 1–4) para encher `java-fixtures/output/`:

```bash
mvn -q package
java -jar target/solid-static-analysis.jar --all "$(pwd)/src/test/resources/java-fixtures" \
  --output "$(pwd)/src/test/resources/java-fixtures/output" --clustering
```

Só algoritmos (com `graphs/` e JSON de scan já existentes):

```bash
java -jar target/solid-static-analysis.jar --analyze "$(pwd)/src/test/resources/java-fixtures/output"
java -jar target/solid-static-analysis.jar --analyze "$(pwd)/src/test/resources/java-fixtures/output" --clustering
```

## Como correr os testes

```bash
mvn test
```

Por defeito o Surefire **exclui** testes com a tag JUnit `clustering` (integração pesada opcional).
Para correr também esses testes:

```bash
mvn test -Dsurefire.excludedGroups=
```

### Fixtures (`java-fixtures`)

- Fontes: `src/test/resources/java-fixtures/*.java`.
- **`java-fixtures/output/`**: opcionalmente versionado como referência (JSON, grafos, algoritmos, scoring); os testes também geram dados em **`target/java-fixtures-pipeline-cache/`** quando precisam sem depender só do disco.

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
- **003 — Algoritmos de grafos**: [specs/003-analyze-graph-algorithms/spec.md](specs/003-analyze-graph-algorithms/spec.md),
  [quickstart.md](specs/003-analyze-graph-algorithms/quickstart.md), [tasks.md](specs/003-analyze-graph-algorithms/tasks.md)
- **004 — Scoring SOLID**: [specs/004-solid-scoring/spec.md](specs/004-solid-scoring/spec.md),
  [quickstart.md](specs/004-solid-scoring/quickstart.md), [contracts/score-cli.md](specs/004-solid-scoring/contracts/score-cli.md)

# Benchmarks (feature 005)

Cada **subpasta direta** de `benchmarks/` é um projeto Java independente para o pipeline (`--all`). Não há pasta agregadora única: os casos SOLID de referência estão segmentados por princípio (pastas `single_responsibility_*`, `open_closed_*`, etc.), e os supermercados são `bad-project/` e `good-project/`.

| Pasta | Conteúdo |
|--------|-----------|
| **`bad-project/`** | Supermercado com marcadores `SOLID-VIOLATION: <letra>`. |
| **`good-project/`** | Mesmo domínio com `domain/`, `repository/`, `usecase/`, `service/`; sem marcadores. |
| **`single_responsibility_*`**, **`open_closed_*`**, **`liskov_substitution_*`**, **`interface_segregation_*`**, **`dependency_inversion_*`** | Pares bad/good por princípio (referência estilo hdeiner/SOLID). |

Estes árvores **não** estão no classpath Maven; servem para corridas manuais do analisador.

**Não** grave saída dentro de `benchmarks/…/output/`. O destino é sempre **`output/` na raiz do repositório** (com `user.dir` = raiz do repo).

## Organização da saída

1. **Um diretório por benchmark** — Com o comando na **raiz do repo**, `--all` sem `--output` grava em `output/<nome-da-pasta>/` (caminho relativo ao repo), por exemplo `output/good-project/`.

2. **JSON e artefatos espelham só o caminho “lógico”** — Prefixos habituais `src/main/java` e `src/test/java` **não** são repetidos no output: por exemplo `…/src/main/java/com/example/Foo.java` gera `…/com/example/Foo.json`, e grafos/scoring/results seguem esse espelho (ex.: `graphs/com/example/.../g3_method_calls/Foo.dot`). Pastas como `repository/` ou `domain/` no output correspondem ao **pacote Java** (`com.example…repository`), não a uma segunda cópia da árvore `src/`.

## Regenerar todos os benchmarks

```bash
cd /caminho/para/solid-static-analysis
mvn -q package   # obrigatório para atualizar target/solid-static-analysis.jar
./benchmarks/run-all.sh
```

Ou um projeto só:

```bash
java -jar target/solid-static-analysis.jar --all "$(pwd)/benchmarks/good-project"
```

Louvain **por defeito**; use `--no-clustering` se quiser só métricas.

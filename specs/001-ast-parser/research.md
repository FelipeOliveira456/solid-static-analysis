# Research: Parser AST (001-ast-parser)

**Branch**: `001-ast-parser` | **Date**: 2026-03-20

## 1. Versões Maven (JavaParser + testes + JSON)

**Decision**: Alinhar `javaparser-core` e `javaparser-symbol-solver-core` à **mesma versão**;
usar **JUnit 5** (BOM `junit-bom` ou `junit-jupiter` explícito); serializar o modelo de saída com
**Jackson** (`jackson-databind`).

**Rationale**: Versões mistas do JavaParser causam erros de linkage; JUnit 5 é exigência da
constituição; Jackson integra bem com POJOs do modelo exportado e evita depender do dump bruto da
AST para o contrato JSON da feature.

**Alternatives considered**:

- **Apenas JSONPrinter / serialização nativa da AST**: rejeitado — artefato seria enorme, instável
  entre versões e desalinhado com FR-005 (modelo de domínio enxuto).
- **Gson**: viável; Jackson escolhido por ecossistema comum em ferramentas Java e tipagem de
  campos opcionais claros.

**Versions pinned in plan**: `javaparser-core` / `javaparser-symbol-solver-core` **3.26.3** (Java 17
compatível, estável); `junit-jupiter` **5.10.2**; `jackson-databind` **2.17.2**. *(Ajustar minor
patch no `pom.xml` se necessário para CVEs; manter JavaParser core + solver na mesma versão.)*

---

## 2. Configuração do symbol solver (`CombinedTypeSolver`)

**Decision**: Instanciar `CombinedTypeSolver` com, no mínimo:

- `ReflectionTypeSolver` (tipos do JDK em classpath da ferramenta);
- `JavaParserTypeSolver` apontando para o **diretório raiz absoluto** recebido na CLI (todo o
  projeto fonte como árvore de fontes).

Opcional na mesma iteração: `JarTypeSolver` para `.jar` sob a raiz, se tempo permitir; caso
contrário documentar limitação (tipos só em `.java` + JDK).

**Rationale**: FR-006 exige resolução de tipos definidos sob a raiz; o padrão documentado no wiki do
JavaParser combina reflexão + fontes.

**Alternatives considered**:

- **Só `JavaParserTypeSolver`**: insuficiente para `String`, `List`, etc. sem reflexão.
- **MavenDependencyResolver**: útil para dependências externas; fora do mínimo do spec (escopo
  pode crescer na iteração 2+).

---

## 3. Estratégia de nomes em `output/` (colisão de basename)

**Decision**: Nome do ficheiro JSON = **caminho relativo à raiz de escaneamento**, com separadores
normalizados para um único token seguro (ex.: `path/to/Foo.java` → `path__to__Foo.java.json` sob
`output/`), garantindo unicidade (FR-004).

**Rationale**: Cumpre colisão `Foo.java` em pacotes diferentes sem inventar pastas espelhadas
completas no output.

**Alternatives considered**:

- **Só basename**: rejeitado — viola FR-004 em monorepos.
- **Espelhar árvore de pastas sob `output/`**: válido; mais I/O de diretórios; token único é mais
  simples para testes e inspeção.

---

## 4. Diretório `output/` e CWD

**Decision**: Diretório de saída = `Paths.get(System.getProperty("user.dir")).resolve("output")`,
criado se não existir; documentar em `quickstart.md` que a execução **deve** ser feita com CWD na
raiz do repositório `solid-static-analysis` (ou ajustar variável de ambiente futura — fora do
mínimo).

**Rationale**: FR-003 referencia `output/` na raiz do repositório da ferramenta; com `java -jar`
o CWD costuma ser onde o utilizador corre o comando — alinhar documentação evita ambiguidade.

**Alternatives considered**:

- **Relativo ao JAR**: exige `CodeSource` / `ProtectionDomain`; mais frágil em IDEs; adiar.

---

## 5. Extração de chamadas e tipos (falhas de resolução)

**Decision**: Para cada `MethodCallExpr` no corpo de um método, tentar `resolve()` via
`JavaSymbolSolver`; em falha, emitir objeto de chamada com `resolved: false` e **string do
escopo** (`toString()` ou nome simples) para não abortar o ficheiro.

**Rationale**: Symbol solver não é totalmente robusto (generics, edge cases); FR-007 exige
continuidade.

**Alternatives considered**:

- **Omitir chamadas não resolvidas**: perde informação útil para análise heurística posterior.

---

## 6. Empacotamento executável

**Decision**: `maven-shade-plugin` (ou `maven-assembly-plugin`) com `Main-Class` apontando para a
classe `main` do scanner (ex. `com.solidanalysis.scanner.ScannerCli`).

**Rationale**: Alinha com o exemplo `java -jar solid-static-analysis.jar <abs-path>`.

---

## Resolução de NEEDS CLARIFICATION

Nenhum marcador pendente no spec; decisões acima fecham ambiguidades operacionais (output path,
colisão de nomes, falhas de resolução).

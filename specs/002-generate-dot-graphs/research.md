# Research: Geração de grafos DOT (002)

**Feature**: `002-generate-dot-graphs`  
**Date**: 2026-03-20

## R1 — Entrada JSON e desserialização

**Decision**: Desserializar cada `*.json` do diretório do projeto para POJOs alinhados ao contrato `ast-artifact.schema.json` (Etapa 1), usando Jackson `ObjectMapper` já presente no `pom.xml`.

**Rationale**: Evita nova dependência; o schema da Etapa 1 é a fonte de verdade; facilita validação e evolução paralela dos DTOs.

**Alternatives considered**:

- Parser JSON genérico (`JsonNode`) — mais flexível a campos opcionais, porém mais frágil e verboso para grafos tipados.
- Gson — duplicaria stack de serialização já padronizada em Jackson.

## R2 — Formato de saída DOT

**Decision**: Gerar texto DOT com `digraph` / `graph` conforme o caso, nós com IDs estáveis (escapados/sanitizados), atributos `label`, `shape`, e arestas com `label`. Sem biblioteca Graphviz em tempo de execção — apenas ficheiros válidos para `dot -Tpng`.

**Rationale**: Dependência zero adicional; controlo total sobre labels exigidos pela spec (field, param, extends, read/write, then/else/loop); ficheiros pequenos e previsíveis.

**Alternatives considered**:

- Biblioteca Java que invoca `dot` via processo — desnecessário para gerar apenas `.dot`.
- API de alto nível (JGraphT + export) — mais peso e menos controlo direto das convenções de ficheiro.

## R3 — CLI e ponto de entrada único

**Decision**: Estender o JAR sombreado com um modo explícito `--graphs <DIR_PROJETO>` onde `DIR_PROJETO` é o diretório já contendo `*.json` (ex.: `output/meu-projeto`). O modo atual da Etapa 1 permanece: um único argumento absoluto = raiz a escanear. Implementação: classe de entrada que faz parse mínimo de `args` e delega a `ScannerCli` ou a um novo `GraphsCli` / `GraphGenerationRunner`.

**Rationale**: Alinha com a invocação pedida (`java -jar ... --graphs /caminho`); um único artefacto Maven.

**Alternatives considered**:

- Classe `main` separada e segundo artefacto — viola simplicidade do shade único.
- Subcomandos estilo picocli — útil a médio prazo; para esta iteração, parse manual de 2–3 argumentos é suficiente (sem nova dependência obrigatória).

## R4 — Filtro de tipos e `System.out`

**Decision**: Centralizar regras em um componente (ex. `ProjectTypeFilter` / `TypeRelevanceRules`): excluir primitivos e `void`; nomes `java.*`, `javax.*`; tratar acesso a `System.out` como não gerador de nós/arestas nos grafos estruturais (alinhado à Etapa 1 / requisitos do utilizador).

**Rationale**: Uma única fonte de verdade evita divergência entre G1–G7.

**Alternatives considered**:

- Duplicar filtros em cada gerador — maior risco de regressão e testes repetidos.

## R5 — Identificação de métodos e construtores

**Decision**: Nó de método no formato `SimpleClassName.methodName` com construtores usando `returnType` igual a `<init>` (como na AST enriquecida). Resolução de chamadas: `declaringType` + nome derivado de `signature` ou convenção documentada no `data-model.md` quando `signature` for parcial.

**Rationale**: Compatível com o modelo já emitido pelo `AstExtractor`.

**Alternatives considered**:

- Assinatura Java completa em cada nó — mais verboso e difícil de ler em Graphviz; pode ser opcional em label, não em ID.

## R6 — G7 CFG e estrutura encadeada

**Decision**: Para cada `controlFlowStatement` com `kind` condicional/loop, emitir nó com `label` baseado em `condition` (ou kind se vazio); arestas `then`, `else`, `loop` conforme presença de `thenLine`/`elseLine` e `chainedElseIf`; tratar `chainedElseIf` como continuação da cadeia no mesmo ficheiro DOT do método.

**Rationale**: Espelha o modelo `controlFlowStatementSummary` do schema (incl. `chainedElseIf`).

**Alternatives considered**:

- Um único nó por método — não cumpre o requisito de branches.

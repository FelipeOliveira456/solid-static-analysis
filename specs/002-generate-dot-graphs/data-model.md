# Data Model: Geração de grafos DOT (002)

**Feature**: `002-generate-dot-graphs`  
**Input contract**: `specs/001-ast-parser/contracts/ast-artifact.schema.json` (um ficheiro JSON = um `AstArtifact` por unidade de compilação / ficheiro fonte).

## 1. Visão geral

| Conceito | Descrição |
|----------|-----------|
| **AstArtifact** | Raiz JSON: `sourceFile`, `primaryType` (`TypeSummary`). |
| **TypeSummary** | `kind`, `name`, `superclass`, `implementedInterfaces`, `fields`, `methods`. |
| **ParsedProject** | Agregação em memória: todos os `AstArtifact` carregados de um diretório + índices derivados. |
| **GraphModel** (por gerador) | Conjunto de nós e arestas lógicas antes da serialização DOT. |
| **DotDocument** | Texto final `.dot` (ou vários ficheiros para G7). |

## 2. AstArtifact (entrada)

Campos relevantes para grafos (espelho lógico do schema; implementação = records/POJOs Jackson):

- **sourceFile**: string — metadado; pode aparecer em comentário DOT opcional.
- **primaryType**: tipo cuja análise estrutural alimenta G1–G6.

### TypeSummary

- **name**: nome simples ou qualificado conforme Etapa 1 (deve ser consistente entre ficheiros do mesmo projeto).
- **kind**: `class` | `interface`.
- **superclass**: `null` ou nome do tipo pai.
- **implementedInterfaces**: lista de nomes.
- **fields**: `name`, `type`.
- **methods**: ver abaixo.

### MethodSummary

- **name**, **returnType** (`<init>` para construtor).
- **parameters**: `name`, `type`.
- **methodCalls**: `declaringType`, `signature`, `expression`, `resolved`.
- **fieldAccesses**: `fieldName`, `ownerClass`, `accessType` (`read` | `write`).
- **instantiations**: `type`, `line`.
- **controlFlowStatements**: árvore/ lista com `kind`, `condition`, linhas, `cases`, `chainedElseIf`.

## 3. Índices derivados (ParsedProject)

Construídos após carregar todos os JSONs:

- **typesByName**: `Map<String, TypeSummary>` (chave = nome usado nas arestas; se colisão simples vs qualificado, política documentada na implementação: preferir nome como no JSON).
- **methodKeys**: conjunto de identificadores `OwnerSimpleName.methodName` para validar alvos de G3.
- **interfaceNames**: conjunto de tipos com `kind == interface`.
- **classNames**: conjunto de tipos com `kind == class`.

## 4. Regras de filtro (tipo relevante)

Um tipo **T** é **incluído** no grafo se:

- Não é primitivo nem `void`.
- Não começa com `java.` nem `javax.`.
- **E** T aparece como nome de tipo declarado no projeto **ou** como alvo de dependência interna após filtragem (política: apenas nós que são tipos do projeto analisado + arestas entre eles).

**System.out**: qualquer field access ou chamada cujo alvo seja a saída padrão é **ignorada** para efeitos de nós e arestas.

## 5. Modelos lógicos por grafo

### G1 — Dependência entre classes

- **Nó**: nome de tipo (classe/interface) do projeto.
- **Aresta**: `from` → `to`, **label** ∈ { `field`, `param`, `return`, `call`, `instantiation` } (múltiplas arestas paralelas ou label composto por convenção em `contracts/dot-graph-output.md`).

### G2 — Herança

- **Aresta**: `child` → `parent`, **label** `extends` ou `implements`.

### G3 — Chamadas de métodos

- **Nó**: `Class.method` (construtor: `returnType` `<init>`).
- **Aresta**: caller → callee quando `methodCalls` resolve para método de tipo do projeto.

### G4 — Uso de atributos + projeção

- **Nós bipartidos**: métodos `Class.method` e campos `Class.field`.
- **Aresta método–campo**: label `read` | `write`.
- **Projeção método–método** (`g4_method_projection.dot`): aresta entre métodos que partilham pelo menos um campo de acesso (mesmo `ownerClass.fieldName`).

### G5 — Interface–implementação

- **Nós**: interfaces (shape ellipse) e classes (shape box).
- **Aresta**: classe → interface quando `implementedInterfaces` contém a interface.

### G6 — Uso de interface

- **Nós**: classes clientes e interfaces.
- **Aresta**: classe → interface quando a interface aparece em tipo de campo, tipo de parâmetro, ou `declaringType` de `methodCalls` (filtrado a tipos do projeto).

### G7 — CFG simplificado

- **Um ficheiro por método** com `controlFlowStatements` não vazio: `g7_cfg/<Class>_<method>.dot` (detalhe de nome em contrato DOT).
- **Nó**: ponto de decisão; **label**: `condition` ou fallback.
- **Aresta**: `then`, `else`, `loop` conforme spec.

## 6. Validação

- JSON inválido: registo de erro e continuação ou falha global — a definir na implementação; testes unitários usam apenas objetos em memória (constituição).

## 7. Determinismo

Mesma entrada → mesma ordenação de nós/arestas (ex.: ordenar por nome/linha) para diffs estáveis.

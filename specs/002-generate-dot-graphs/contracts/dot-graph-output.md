# Contract: Convenções dos ficheiros DOT

**Feature**: `002-generate-dot-graphs`

## Formato geral

- Ficheiros UTF-8.
- Grafo dirigido: `digraph G { ... }` salvo indicação contrária.
- IDs de nós: identificadores DOT válidos; caracteres especiais escapados ou substituídos (ex.: `"` → `\"` em labels).

## G1 — `g1_dependency.dot`

- Nós: tipos do projeto (classes e interfaces).
- Arestas: `A -> B [label="field"]` (valores de label: `field`, `param`, `return`, `call`, `instantiation`).
- Múltiplas relações mesmo par (A,B): permitido com arestas distintas ou label combinado — implementação deve ser determinística.

## G2 — `g2_inheritance.dot`

- Arestas: `label="extends"` ou `label="implements"`.

## G3 — `g3_method_calls.dot`

- Nós: `Owner.methodName` (construtores com método sintético conforme modelo AST, ex. nome da classe e `<init>` no metadata do nó ou no label).
- Arestas sem label obrigatório além do necessário para leitura; opcional: `label` com expressão da chamada.

## G4 — `g4_field_usage.dot` e `g4_method_projection.dot`

- **field_usage**: nós com `shape` distinto ou prefixo de label para distinguir método vs campo (ex.: label do nó = string completa `Class.field` / `Class.method`).
- Arestas método→campo: `label="read"` ou `label="write"`.
- **method_projection**: apenas nós método; aresta entre métodos que acedem ao mesmo campo (mesmo par owner+name).

## G5 — `g5_interface_impl.dot`

- Interface: `shape=ellipse` (ou `ellipse` no DOT).
- Classe: `shape=box`.
- Aresta: implementação `class -> interface`.

## G6 — `g6_interface_usage.dot`

- Grafo dirigido classe → interface conforme usos em campo, parâmetro e `declaringType` de chamadas.

## G7 — `g7_cfg/<Class>_<method>.dot`

- Nome do ficheiro: classe e método seguros para filesystem (substituir caracteres inválidos).
- Nós: decisões; label textual = `condition` quando existir.
- Arestas: `label="then"`, `label="else"`, `label="loop"` conforme ramos presentes; suportar `chainedElseIf` como extensão da cadeia.

## Visualização

Todos os ficheiros MUST abrir com:

```bash
dot -Tpng arquivo.dot -o arquivo.png
```

sem erros de parse (Graphviz instalado no ambiente do utilizador).

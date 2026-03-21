# Data model: AST export (001-ast-parser)

**Branch**: `001-ast-parser` | **Spec**: [spec.md](./spec.md)

Modelo lógico serializado em JSON por ficheiro `.java` processado. Não expõe a AST interna do
JavaParser; é um **DTO estável** para etapas seguintes.

## Entidades

### `AstArtifact` (raiz do ficheiro `.json`)

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `sourceFile` | string | sim | Caminho absoluto do `.java` de entrada |
| `primaryType` | `TypeSummary` | sim | Tipo principal (primeira declaração top-level tipo ou interface; se múltiplas, documentar no plano de código: primeira ou erro — **implementação: primeira `ClassOrInterfaceDeclaration`**) |

### `TypeSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `kind` | enum string | sim | `"class"` ou `"interface"` |
| `name` | string | sim | Nome simples |
| `abstract` | boolean | sim | `abstract` ou interface → `true` para interface |
| `superclass` | string \| null | sim | Nome qualificado ou simples resolvido quando possível; `null` se nenhuma ou `java.lang.Object` omitido |
| `implementedInterfaces` | string[] | sim | Lista (pode ser vazia) |
| `fields` | `FieldSummary[]` | sim | |
| `methods` | `MethodSummary[]` | sim | |

### `FieldSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `name` | string | sim | |
| `type` | string | sim | Representação legível (resolvida quando possível; senão texto da AST) |

### `MethodSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `name` | string | sim | |
| `returnType` | string | sim | Inclui `void` |
| `parameters` | `ParameterSummary[]` | sim | Ordem de declaração |
| `methodCalls` | `MethodCallSummary[]` | sim | Chamadas (`MethodCallExpr`) e construtores (`new ...` / `ObjectCreationExpr`) no corpo do método, ordenados por posição no fonte; mesma forma `expression` / `resolved` / assinatura |
| `controlFlowStatements` | `ControlFlowStatementSummary[]` | sim | Estruturas de fluxo no corpo (`if`, `while`, `for`, `try`, …), ordenadas por posição no ficheiro |
| `fieldAccesses` | `FieldAccessSummary[]` | sim | Acessos a campos resolvidos (`FieldAccessExpr` e `NameExpr` campo); omitidos quando o symbol solver não resolve |

### `FieldAccessSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `fieldName` | string | sim | Nome do campo |
| `ownerClass` | string | sim | Nome qualificado do tipo declarante |
| `accessType` | string | sim | `read` ou `write` (alvo de `AssignExpr`) |

### `ControlFlowStatementSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `kind` | string | sim | Ex.: `if`, `while`, `doWhile`, `for`, `foreach`, `switch`, `synchronized`, `try`, `catch` |
| `condition` | string \| null | não | Texto da condição / selector / tipo em `catch`; `null` em `try` |
| `line` | int \| null | não | Linha aproximada no fonte (1-based) |
| `thenLine` | int \| null | não | Início do ramo then / corpo do loop (`if`, `while`, `for`, `foreach`); omitido quando não aplicável |
| `elseLine` | int \| null | não | Início do `else` em `if`; omitido se não houver `else` |
| `endLine` | int \| null | não | Fim do constructo (`if`, `while`, `for`, `foreach`, `switch`) |
| `cases` | `SwitchCaseSummary[]` | não | Só em `switch`: cada entrada com `label` e `line` |
| `chainedElseIf` | `ControlFlowStatementSummary` | não | Só em `if`: ramo `else if (...)` seguinte, encadeado (mutuamente exclusivo com o `if` anterior); recursivo para `else if` múltiplos |

### `SwitchCaseSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `label` | string | sim | Rótulo(s) do `case` ou `default` |
| `line` | int \| null | não | Linha da entrada no fonte |

### `ParameterSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `name` | string | sim | |
| `type` | string | sim | |

### `MethodCallSummary`

| Campo | Tipo | Obrigatório | Regras |
|-------|------|-------------|--------|
| `expression` | string | sim | Texto ou nome qualificado quando resolvido |
| `resolved` | boolean | sim | `true` se `resolve()` do symbol solver tiver sucesso |
| `declaringType` | string \| null | não | Preenchido quando `resolved` e disponível |
| `signature` | string \| null | não | Assinatura amigável quando resolvido |

## `ScanRunResult` (apenas em memória / não serializado em `.json`)

Usado pelo CLI para agregar execução:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `successCount` | int | Ficheiros com artefato escrito |
| `failureCount` | int | Ficheiros com falha de parse ou I/O |
| `failures` | lista de paths | Para log em stdout (FR-007) |

## Validação

- Campos obrigatórios sempre presentes no JSON (valores vazios como `[]` ou `null` explícito onde
  indicado).
- Encoding UTF-8 ao ler fontes e escrever JSON.

## Relacionamentos

- Um `AstArtifact` por ficheiro `.java` bem-sucedido.
- `MethodSummary.methodCalls` são filhos lógicos do método; não há grafo global nesta etapa.

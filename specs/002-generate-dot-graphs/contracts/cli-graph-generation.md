# Contract: CLI — geração de grafos (`--graphs`)

**Feature**: `002-generate-dot-graphs`  
**Artifact**: `solid-static-analysis` shaded JAR (mesmo `finalName` Maven da Etapa 1).

## Invocação

```bash
java -jar solid-static-analysis.jar --graphs <PROJECT_OUTPUT_DIR>
```

- **PROJECT_OUTPUT_DIR**: caminho absoluto ao diretório do projeto que **já contém** os ficheiros `*.json` produzidos pela Etapa 1 (ex.: `.../output/meu-projeto`).
- Comportamento recomendado: rejeitar caminho relativo com mensagem clara (alinhado ao modo scan atual).

## Saída

- Cria (se necessário) `<PROJECT_OUTPUT_DIR>/graphs/`.
- Escreve:
  - `g1_dependency.dot`
  - `g2_inheritance.dot`
  - `g3_method_calls.dot`
  - `g4_field_usage.dot`
  - `g4_method_projection.dot`
  - `g5_interface_impl.dot`
  - `g6_interface_usage.dot`
  - `g7_cfg/<Class>_<method>.dot` (apenas métodos com pelo menos um `controlFlowStatement`).

## Códigos de saída

| Código | Significado |
|--------|-------------|
| 0 | Geração concluída (avisos opcionais em stderr) |
| 1 | Argumentos inválidos, diretório inexistente, ou falha fatal de I/O |

## Compatibilidade com Etapa 1

```bash
java -jar solid-static-analysis.jar <ABS_ROOT_DIR>
```

Continua a significar: varrer projeto Java e escrever JSON em `output/` relativo ao CWD (comportamento atual), **sem** gerar grafos.

## Erros

- Diretório sem `*.json`: processo termina com sucesso e produz grafos vazios ou ficheiros DOT mínimos válidos (sem crash), conforme `spec.md` edge cases.

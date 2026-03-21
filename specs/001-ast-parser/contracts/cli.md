# CLI contract: solid-static-analysis scanner

**Feature**: `001-ast-parser` | **Spec**: [spec.md](../spec.md)

## Invocação

```text
java -jar solid-static-analysis.jar <ABS_ROOT_DIR>
```

| Argumento | Obrigatório | Descrição |
|-----------|-------------|-----------|
| `ABS_ROOT_DIR` | sim | Caminho **absoluto** do diretório raiz do projeto Java a escanear |

## Comportamento

1. Valida que existe exatamente um argumento e que o caminho é absoluto e existe como
   diretório; caso contrário mensagem em **stderr** (ou stdout conforme implementação única
   documentada) e **exit code ≠ 0**.
2. Percorre `ABS_ROOT_DIR` recursivamente por ficheiros `*.java`.
3. Para cada ficheiro: parse + extração + escrita em `output/` (ver [ast-artifact.schema.json](./ast-artifact.schema.json)).
4. Em falha por ficheiro: imprime identificação do ficheiro em **stdout** (alinhado a FR-007) e
   continua.
5. Ao terminar: imprime linha(s) de resumo com contagens de sucesso e falha (FR-008).

## Exit codes

| Código | Situação | Significado |
|--------|----------|-------------|
| `0` | Scan executado | Processo completou o walk; podem existir falhas parciais por ficheiro (ver resumo em stdout). **Árvore sem `.java`** é sucesso com zero parseados. |
| `≠ 0` | Erro fatal antes do scan útil | Número de argumentos ≠ 1; caminho não absoluto; caminho inexistente; não é diretório; falha ao criar/escrever `output/` quando necessário; outras falhas irrecuperáveis documentadas no `README.md`. |

**Regra**: argumentos inválidos → **stderr** + exit `≠ 0`. Falhas **por ficheiro** durante o scan
→ stdout + continuar + exit `0` ao terminar (alinhado a FR-007/FR-008).

*Nota*: “Nenhum `.java` encontrado” ≠ erro fatal: exit `0` com resumo coerente (ex. `Parsed: 0`).

## Saída padrão (stdout)

- Linhas de erro por ficheiro (caminho + mensagem curta opcional).
- Linha final com totais, formato legível, por exemplo:
  - `Parsed: 12, Failed: 1`

## Diretório de saída

- Artefatos em `{user.dir}/output/` relativamente ao processo (ver [research.md](../research.md));
  utilizador deve executar com CWD na raiz do repositório da ferramenta salvo documentação
  alternativa.

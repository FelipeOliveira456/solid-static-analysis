# Contract: CLI `--analyze` (Etapa 3)

## Command

`java -jar solid-static-analysis.jar --analyze <ABS_OUTPUT_PROJECT_DIR>`

## Input contract

- `<ABS_OUTPUT_PROJECT_DIR>` deve ser um caminho absoluto para `output/<projeto>`.
- O diretório deve conter `graphs/` com os arquivos `.dot` gerados na Etapa 2.
- A etapa 3 deve identificar automaticamente:
  - G1: `graphs/g1_dependency.dot`
  - G2: `graphs/g2_inheritance.dot`
  - G3: `graphs/g3_method_calls/<Classe>.dot`
  - G4 field: `graphs/g4_field_usage/<Classe>.dot`
  - G4 projection: `graphs/g4_method_projection/<Classe>.dot`
  - G5: `graphs/g5_interface_impl.dot`
  - G6: `graphs/g6_interface_usage.dot`
  - G7: `graphs/g7_cfg/<Classe>_<Metodo>.dot`

## Output contract

- Os resultados devem ser persistidos em `output/<projeto>/algorithms/`.
- A etapa deve escrever arquivos JSON conforme:
  - `algorithms/g1_algorithms.json`
  - `algorithms/g2_algorithms.json`
  - `algorithms/g3_algorithms/<Classe>.json`
  - `algorithms/g4_field_algorithms/<Classe>.json`
  - `algorithms/g4_projection_algorithms/<Classe>.json`
  - `algorithms/g5_algorithms.json`
  - `algorithms/g6_algorithms.json`
  - `algorithms/g7_algorithms/<Classe>_<Metodo>.json`

## Failure contract

- Se o diretório de entrada nao existir ou nao for um diretorio valido, a execucao deve falhar com:
  - mensagem clara indicando o problema
  - codigo de saida nao-zero
- Se um subconjunto de grafos estiver ausente, a etapa deve:
  - executar analises para os grafos presentes
  - registrar mensagens claras sobre itens ausentes

## Determinism

- Para entradas equivalentes (mesmos grafos `.dot`), os JSONs devem ser deterministicos.


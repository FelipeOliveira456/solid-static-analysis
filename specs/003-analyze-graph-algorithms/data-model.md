# Data Model: Graph Algorithms Analysis (Etapa 3)

## Entidades principais

- `AnalyzeRequest`
  - `projectOutputDir`: caminho para `output/<projeto>` (origem dos grafos em `graphs/` e destino de escrita em `algorithms/`).

- `GraphInput`
  - `graphId`: identificador lógico (`G1`, `G2`, `G3`, `G4_field`, `G4_projection`, `G5`, `G6`, `G7`).
  - `dotPath`: caminho absoluto para o arquivo `.dot` no disco.
  - `directed`: booleano (grafo direcionado vs não-direcionado).
  - `nodePrefixRules`: regras de filtragem (ex.: `s_` em G7; `m_`/`f_` em G4).

- `LoadedGraph`
  - instancia de grafo em memoria (JGraphT) carregada via `DOTImporter`/modelagem equivalente.

- `AlgorithmRunner`
  - orquestra a execução dos algoritmos por grafo e a serializacao dos resultados.

## Modelos de resultado (alto nível)

As estruturas abaixo representam “contratos de dado” para os JSONs de saída. A forma exata dos campos pode ser refinada na implementação, mas a estrutura deve preservar as convenções do arquivo (nomes e diretórios) exigidas pela especificação.

- `SccResult` (G1 e G3)
  - `components`: lista de componentes, cada componente contendo:
    - `nodes`: lista de nós no componente
    - `size`: tamanho do componente

- `DegreeMetricsResult` (in-degree, out-degree e centralidade)
  - `inDegreeByNode`: mapa `node -> value`
  - `outDegreeByNode`: mapa `node -> value`
  - `centralityByNode`: mapa `node -> value` (formula de degree centrality para dirigidos)

- `LongestPathDagResult` (G2 e G7)
  - `roots`: lista de nós raiz (in-degree zero)
  - `maxDepthByRoot`: mapa `root -> maxDepth`
  - `globalMaxDepth`: maior profundidade observada no grafo
  - `dagConstraintViolated`: booleano/flag para reportar incapacidade caso a estrutura nao permita uma ordenacao topologica confiavel

- `IsolatedNodesResult` (G3 e G4_projection/G4 quando aplicavel)
  - `isolatedNodes`: lista de nós isolados conforme regra do grafo

- `ConnectedComponentsResult` (G4_projection)
  - `components`: lista de componentes com:
    - `nodes`: lista de nós no componente
    - `size`: tamanho do componente

- `LcomResult` (G4 field usage)
  - `lcom`: valor entre `0.0` e `1.0`
  - `methodPairStats` (opcional): contagens/denominador numerico usado na proporcao, para facilitar validacao em testes

- Particao Louvain: campo `clusters` (lista de listas de nos) em G1, G3 e G4_projection quando `--clustering` esta ativo
  - `runs`: lista de resultados por `k`, cada item contendo:
    - `k`
    - `components`: componentes resultantes (conjuntos de nós)

- `G7CfgResult` (G7 por classe/metodo)
  - `decisionNodes`: lista/contagem de nós de decisao (prefixo `s_`, excluindo `entry`, `then_`, `else_`)
  - `maxDecisionOutDegree`
  - `longestPathFromEntryDepth`: profundidade maxima calculada do `entry`

## Entidades de serializacao e nomeacao

- JSONs devem seguir a estrutura de diretórios e nomes definida pela especificação:
  - `output/<projeto>/algorithms/g1_algorithms.json`
  - `output/<projeto>/algorithms/g2_algorithms.json`
  - `output/<projeto>/algorithms/g3_algorithms/<Classe>.json`
  - `output/<projeto>/algorithms/g4_field_algorithms/<Classe>.json`
  - `output/<projeto>/algorithms/g4_projection_algorithms/<Classe>.json`
  - `output/<projeto>/algorithms/g5_algorithms.json`
  - `output/<projeto>/algorithms/g6_algorithms.json`
  - `output/<projeto>/algorithms/g7_algorithms/<Classe>_<Metodo>.json`


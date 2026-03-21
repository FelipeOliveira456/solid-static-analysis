# Research: Graph Algorithms Analysis (Etapa 3)

## Scope
Esta etapa executa algoritmos sobre grafos `.dot` gerados na Etapa 2 e salva resultados em JSON em `output/<projeto>/algorithms/`.

## Decision
1. **Carregamento de grafos**: usar JGraphT com `DOTImporter` em `DirectedPseudograph` para grafos dirigidos (suporta self-loops e arestas paralelas, ex. G1) e `Multigraph` para G4 projeção não-direcionada. Interfaces G5 (`shape=ellipse`) complementadas por leitura textual do DOT quando necessário.
2. **SCC**: usar SCC via algoritmo de Kosaraju para identificar componentes fortemente conectadas; componente com tamanho > 1 indica ciclo.
3. **Graus e centralidade**: calcular in-degree e out-degree diretamente na interface `Graph` e calcular degree centrality manualmente com a formula:
   - `centrality(v) = (inDegree(v) + outDegree(v)) / (2 * (n-1))`
4. **Longest path em DAG**: não usar “longest path” nativo; percorrer em ordem topológica e acumular distâncias a partir dos nós com in-degree zero, retornando a maior profundidade por raiz.
5. **Nós isolados**: filtrar nós com soma de graus zero para grafos dirigidos e grau zero para grafos não-dirigidos.
6. **Componentes conectados**: usar `ConnectivityInspector` em grafo não-direcionado para obter lista de componentes e seus tamanhos.
7. **LCOM**: calcular manualmente sobre o grafo de uso de campos (G4 field usage) a partir da intersecao entre conjuntos de atributos vizinhos de pares de metodos.
8. **Louvain**: implementacao propria (fases locais + colapso de comunidades, seed fixa para reprodutibilidade). Opcional na execucao (CLI `--clustering`). Aplica-se a G1, G3 e G4 projecao (grafo nao-dirigido ponderado derivado dos DOTs). Sem a flag, o campo `clusters` nos JSONs fica como lista vazia. O numero de comunidades e descoberto pelo algoritmo (sem parametro `k`).

## Rationale
As escolhas acima seguem exatamente as definições da Etapa 3 e garantem que cada métrica possa ser calculada de forma reprodutível a partir apenas do conteúdo dos grafos `.dot`.

## Alternatives Considered
- Usar algoritmo nativo de longest path: rejeitado por inexistência/limitacao para grafos sem garantia de DAG.
- Girvan–Newman (JGraphT): substituido por Louvain proprio com saida `clusters` e seed fixa (FR-012).

# Feature Specification: Graph Algorithms Analysis (Etapa 3)

**Feature Branch**: `003-analyze-graph-algorithms`  
**Created**: 2026-03-21  
**Status**: Draft  
**Input**: User description: "ETAPA 3 - Algoritmos de grafos: carregar DOT gerados na Etapa 2, executar analises (SCC, graus, centralidade, longest path, LCOM, clusterizacao) e salvar resultados em JSON no diretório `algorithms/`."

**Constitution**: Feature branch naming, testing, and layout MUST align with
`.specify/memory/constitution.md` (e.g. `NNN-kebab-case`, JUnit 5, Maven `src/main` / `src/test`).

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - Analyze graphs and generate algorithms JSON (Priority: P1)

O usuario executa o analisador apontando para o diretorio `output/<projeto>` contendo os grafos `.dot` da Etapa 2; o sistema carrega cada grafo, roda os algoritmos da Etapa 3 e salva os resultados em `output/<projeto>/algorithms/`.

**Why this priority**: E o fluxo principal que transforma os grafos em evidencias objetivas para identificar ciclos, profundidade de hierarquia, coesao e agrupamentos naturais.

**Independent Test**: Pode ser testado totalmente executando o analisador em um conjunto fixture com grafos completos e verificando a presenca e validade dos arquivos JSON gerados no diretorio `algorithms/`.

**Acceptance Scenarios**:

1. **Given** um diretorio `output/<projeto>` contendo `graphs/` com os grafos esperados de G1 ate G7, **When** o usuario executa `--analyze <output/<projeto>>`, **Then** o sistema cria os arquivos JSON em `algorithms/` seguindo exatamente as convencoes de nome e subdiretorio por grafo/classe/metodo.
2. **Given** um diretorio `output/<projeto>` contendo apenas um subconjunto de grafos (por exemplo, somente G1 e G2), **When** o usuario executa `--analyze <output/<projeto>>`, **Then** o sistema analisa os grafos presentes e produz apenas as saidas correspondentes, registrando mensagens claras sobre itens ausentes.

---

### User Story 2 - Detect cycles and compute metrics per graph (Priority: P2)

O usuario quer que o sistema identifique ciclos (quando existirem), estime conectividade e centralidade por nos, e calcule profundidade/estruturas de hierarquia onde aplicavel.

**Why this priority**: Esses indicadores sao usados para diagnosticar acoplamento excessivo e entender a estrutura do codigo modelado pelos grafos.

**Independent Test**: Pode ser testado com fixtures de grafos minimos com valores conhecidos, validando as saidas JSON dos algoritmos (por exemplo, SCC indicando ciclo e calculo correto de graus).

**Acceptance Scenarios**:

1. **Given** um grafo de dependencia/modelo com um ciclo identificado (A->B->A), **When** o sistema roda SCC nesse grafo, **Then** a saida indica um componente fortemente conectado contendo exatamente os nos do ciclo.
2. **Given** um grafo direcionado com graus bem definidos (nos com multiplas arestas entrando/saindo), **When** o sistema calcula in-degree, out-degree e centralidade de grau, **Then** os valores persistem corretamente no JSON por no/classe.

---

### User Story 3 - Cohesion and clustering outputs for natural modules (Priority: P3)

O usuario quer que o sistema produza medidas de coesao (LCOM) e detecte agrupamentos naturais de responsabilidade (clusterizacao Louvain, implementacao propria), especialmente nos grafos de uso de campos e projeto de metodos.

**Why this priority**: Essa combinacao fornece evidencias de onde responsabilidades podem estar misturadas ou onde existem modulos naturais.

**Independent Test**: Pode ser testado com grafos unitarios minimos onde a coesao e a separacao entre clusters sao conhecidas, verificando os valores reportados no JSON (incluindo casos com nodos isolados).

**Acceptance Scenarios**:

1. **Given** dois ou mais metodos com conjuntos de atributos totalmente disjuntos, **When** o sistema calcula LCOM no grafo de uso de campos, **Then** o JSON retorna coesao zero (LCOM 1.0) conforme a definicao do requisito.
2. **Given** um grafo com dois grupos bem conectados internamente e poucas arestas entre grupos, **When** o sistema executa Louvain (com seed fixo), **Then** a saida indica clusters coerentes com a separacao esperada e execucoes repetidas com os mesmos dados produzem a mesma particao.

---

Nao ha user stories adicionais para esta etapa.

### Edge Cases

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right edge cases.
-->

- Quando o diretorio `output/<projeto>` nao possui `graphs/` (ou nao possui um grafo esperado), o sistema deve finalizar com mensagem clara e sem gerar saidas parciais silenciosas.
- Quando o grafo possui apenas 0 ou 1 no, algoritmos devem produzir resultados validos (ex.: SCC retorna componentes adequadas, listas de isolados podem ser vazias/contendo apenas o no).
- Quando existem nodos isolados (sem arestas incidentes), as saidas devem listar esses nodos conforme a regra definida por tipo de grafo (direcionado vs nao-direcionado).
- Para metricas que assumem estrutura aciclica (longest path em DAG), quando ciclos impedirem uma ordenacao topologica valida, o sistema deve reportar explicitamente a limitacao e nao produzir um resultado enganoso.
- No grafo G7 (controle de fluxo), a contagem de nodos de decisao deve seguir as regras de filtragem definidas (ex.: considerar prefixos `s_` e excluir nodos reservados como `entry`, `then_` e `else_`).

## Requirements *(mandatory)*

<!--
  ACTION REQUIRED: The content in this section represents placeholders.
  Fill them out with the right functional requirements.
-->

### Functional Requirements

- **FR-001**: O sistema deve permitir ao usuario executar a analise via linha de comando, fornecendo um caminho para `output/<projeto>` e ativando o modo `--analyze`.
- **FR-002**: O sistema deve carregar os grafos `.dot` produzidos na Etapa 2 a partir de `output/<projeto>/graphs/`, identificando quais grafos correspondem a G1, G2, G3, G4, G5, G6 e G7 e respeitando se sao dirigidos ou nao-dirigidos.
- **FR-003**: Para G1 (`g1_dependency.dot`), o sistema deve gerar em `algorithms/`:
  - SCC (componentes fortemente conectados) para detectar ciclos entre classes;
  - in-degree e out-degree por classe;
  - degree centrality por classe usando a formula: `(inDegree(v) + outDegree(v)) / (2 * (n-1))`;
  - clusterizacao Louvain (grafo nao-dirigido derivado das dependencias; campo JSON `clusters`: lista de comunidades, cada uma lista de nos; {@code k} implicito).
- **FR-004**: Para G2 (`g2_inheritance.dot`), o sistema deve gerar em `algorithms/`:
  - longest path a partir de nos raiz (in-degree zero), produzindo profundidade por no raiz conforme definicao por acumulacao maxima em ordem topologica;
  - in-degree por classe para medir quantas classes herdam de cada uma.
- **FR-005**: Para G3 (`g3_method_calls/<Classe>.dot`), o sistema deve gerar em `algorithms/` (por classe):
  - in-degree e out-degree por metodo;
  - SCC para detectar ciclos de chamada;
  - lista de nodos isolados (in-degree + out-degree = 0);
  - clusterizacao Louvain (mesmo esquema `clusters` que G1).
- **FR-006**: Para G4 field usage (`g4_field_usage/<Classe>.dot`), o sistema deve gerar em `algorithms/` (por classe):
  - para metodos: out-degree (quantos atributos acessa);
  - para atributos: in-degree (quantos metodos acessam);
  - LCOM calculado manualmente sobre metodos, conforme: proporcao de pares de metodos sem intersecao entre conjuntos de atributos acessados (0.0 coesao perfeita; 1.0 coesao zero).
- **FR-007**: Para G4 projection (`g4_method_projection/<Classe>.dot`), o sistema deve gerar em `algorithms/` (por classe):
  - componentes conectados (listas de nos por componente e tamanho);
  - lista de nodos isolados (grafo nao-direcionado: grau zero);
  - clusterizacao Louvain no grafo nao-dirigido (campo `clusters`).
- **FR-008**: Para G5 (`g5_interface_impl.dot`), o sistema deve gerar em `algorithms/`:
  - in-degree por interface;
  - out-degree por classe;
  - lista de interfaces com in-degree zero (interfaces nao implementadas no grafo analisado).
- **FR-009**: Para G6 (`g6_interface_usage.dot`), o sistema deve gerar em `algorithms/`:
  - classificar nodos de interface usando a lista de interfaces identificadas em G5;
  - in-degree por interface;
  - lista de interfaces com in-degree zero.
- **FR-010**: Para G7 (`g7_cfg/<Classe>_<Metodo>.dot`), o sistema deve gerar em `algorithms/` (por classe/metodo):
  - contagem de nodos de decisao considerando apenas nos com prefixo `s_`, com filtragem para ignorar `entry`, `then_` e `else_`;
  - longest path a partir do no `entry`, usando percurso em ordem topologica e acumulando profundidades;
  - explosao de branches: estimar o maior out-degree entre os nodos de decisao.
- **FR-011**: O sistema deve salvar os resultados em JSON valido, respeitando a estrutura de diretorios e nomes de arquivos abaixo:
  - `algorithms/g1_algorithms.json`
  - `algorithms/g2_algorithms.json`
  - `algorithms/g3_algorithms/<Classe>.json`
  - `algorithms/g4_field_algorithms/<Classe>.json`
  - `algorithms/g4_projection_algorithms/<Classe>.json`
  - `algorithms/g5_algorithms.json`
  - `algorithms/g6_algorithms.json`
  - `algorithms/g7_algorithms/<Classe>_<Metodo>.json`
- **FR-012**: Para entradas equivalentes (mesmo conjunto de grafos), o sistema deve produzir resultados deterministicos (mesmos valores num algoritmo e mesma classificacao de nodos); o Louvain usa seed fixa para a ordem de visita.
- **FR-013**: O sistema deve tratar entradas inexistentes/invalidas com mensagens claras e comportamento consistente (por exemplo, abortar a analise do grafo afetado e registrar o motivo, sem quebrar as demais analises quando possivel).
- **FR-014**: O projeto deve conter testes automatizados:
  - testes unitarios para cada algoritmo usando grafos minimos construidos em memoria;
  - testes de integracao para verificar que os fixtures de `src/test/resources` geram os JSONs esperados em `output/<...>/algorithms/` (mesma estrutura do ambiente de producao);
  - teste de integracao com a arvore completa de `java-fixtures/output/graphs/` sem clusterizacao (rapido, `mvn test` padrao);
  - teste de integracao com a mesma arvore e Louvain habilitado (marcado {@code @Tag("clustering")}); opcionalmente excluido do `mvn test` padrao por configuracao do Surefire.

### Assumptions
- Os identificadores de nos seguem as convencoes de nomenclatura definidas pela Etapa 2 (ex.: prefixos `m_`, `f_`, `s_`, e nomes reservados como `entry`, `then_`, `else_` no G7).
- O total `n` usado em formulas (centralidade) e o numero de nos do grafo analisado (apenas nos carregados do DOT).
- Quando um algoritmo assume DAG (longest path), presume-se que as entradas mapeadas para aquele grafo respeitam essa propriedade; se nao, o sistema deve reportar a impossibilidade e nao gerar valores inconsistentes.

### Key Entities *(include if feature involves data)*

- **Projeto analisado (`output/<projeto>`)**: diretorio que contem `graphs/` (DOT da Etapa 2) e destino `algorithms/` (JSON da Etapa 3).
- **Grafo DOT**: um artefato `.dot` identificado como G1, G2, G3, G4 (field usage/projection), G5, G6 ou G7, com tipo (dirigido vs nao-dirigido) e conjunto de nos/arestas.
- **Resultado de analise (JSON)**: arquivo persistido por grafo/classe/metodo com metricas especificas (ex.: SCC, in/out-degree, centralidade, LCOM, componentes conectados, clustering e longest path).

## Success Criteria *(mandatory)*

<!--
  ACTION REQUIRED: Define measurable success criteria.
  These must be technology-agnostic and measurable.
-->

### Measurable Outcomes
- **SC-001**: Para um projeto fixture com grafos G1..G7 completos, `output/<projeto>/algorithms/` contem todos os arquivos esperados (contagem por grafo/classe/metodo) e cada arquivo JSON e parseavel.
- **SC-002**: Para fixtures unitarios (grafos minimos) e casos conhecidos, os algoritmos retornam os valores esperados (por exemplo, SCC detectando ciclo com 2 nos, LCOM 1.0 para metodos com atributos disjuntos, e Louvain separando dois grupos densos com poucas arestas entre eles, com reprodutibilidade sob a mesma seed).
- **SC-003**: A analise de um projeto fixture de tamanho pequeno/medio conclui em no maximo 30 segundos em ambiente de desenvolvimento (execucao padrao sem clusterizacao opcional; com `--clustering` em todos os grafos o tempo pode ser maior e e coberto por teste dedicado com timeout proprio).
- **SC-004**: Rodar a analise duas vezes com os mesmos grafos produz resultados numericos equivalentes (mesmos valores/estrutura de saida), garantindo reprodutibilidade.

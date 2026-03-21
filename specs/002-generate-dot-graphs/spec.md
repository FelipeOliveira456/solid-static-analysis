# Feature Specification: Geração de Grafos DOT

**Feature Branch**: `002-generate-dot-graphs`  
**Created**: 2026-03-20  
**Status**: Draft  
**Input**: User description: "ETAPA 2 — GERAÇÃO DE GRAFOS"

**Constitution**: Feature branch naming, testing, and layout MUST align with
`.specify/memory/constitution.md` (e.g. `NNN-kebab-case`, JUnit 5, Maven `src/main` / `src/test`).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gerar todos os grafos estruturais (Priority: P1)

Como pessoa desenvolvedora, quero executar o módulo de geração de grafos sobre um diretório já
analisado para obter automaticamente os arquivos DOT de dependências, herança, chamadas, uso de
atributos, interfaces e fluxo de controle.

**Why this priority**: Este é o valor principal da etapa: transformar os artefatos da etapa anterior
em visualizações estruturais utilizáveis.

**Independent Test**: Pode ser testado executando o gerador em um diretório com JSONs válidos e
verificando se todos os arquivos esperados são criados no diretório `graphs/`, incluindo o
subdiretório `g7_cfg/`.

**Acceptance Scenarios**:

1. **Given** um diretório de projeto contendo JSONs válidos, **When** o gerador é executado,
   **Then** os arquivos `g1_dependency.dot`, `g2_inheritance.dot`, `g3_method_calls.dot`,
   `g4_field_usage.dot`, `g4_method_projection.dot`, `g5_interface_impl.dot`,
   `g6_interface_usage.dot` e os arquivos individuais de `g7_cfg/` são produzidos.
2. **Given** um método com decisões de fluxo, **When** o gerador cria CFGs, **Then** um arquivo DOT
   individual do método é criado com nós e arestas de branches.

---

### User Story 2 - Garantir relevância das dependências (Priority: P2)

Como pessoa desenvolvedora, quero que os grafos ignorem tipos externos e irrelevantes para o
domínio do projeto para evitar ruído na análise.

**Why this priority**: Reduz ruído e melhora utilidade analítica dos grafos para decisões de design.

**Independent Test**: Pode ser testado com entradas contendo tipos primitivos, tipos de biblioteca
padrão e acessos a saída padrão, verificando que não aparecem como nós ou arestas.

**Acceptance Scenarios**:

1. **Given** uma estrutura com campos, parâmetros, retornos, chamadas e instanciações que incluem
   tipos externos, **When** o grafo de dependência é gerado, **Then** somente dependências do próprio
   projeto aparecem.
2. **Given** chamadas para tipos externos em métodos, **When** o grafo de chamadas é gerado,
   **Then** apenas chamadas entre métodos do projeto são conectadas.

---

### User Story 3 - Validar regras por testes unitários (Priority: P3)

Como mantenedor(a), quero uma suíte de testes unitários em memória cobrindo as principais regras de
cada grafo para prevenir regressões.

**Why this priority**: A estabilidade da etapa depende de validação automatizada dos critérios de
formação de arestas, filtros e geração condicional.

**Independent Test**: Pode ser testado executando a suíte de testes unitários e verificando que os
cenários críticos de G1, G2, G3, G4, G5, G6 e G7 são aprovados sem uso de arquivos externos.

**Acceptance Scenarios**:

1. **Given** objetos de AST montados em memória com herança e implementação, **When** o teste de G2
   roda, **Then** as arestas corretas de herança e implementação são encontradas.
2. **Given** métodos com e sem controle de fluxo, **When** o teste de G7 roda, **Then** somente
   métodos com decisões geram artefato de CFG.

---

### Edge Cases

- Diretório de entrada sem arquivos JSON não deve produzir grafos inválidos; o processo deve
  concluir sem falhas inesperadas.
- JSON com campos opcionais ausentes em parte dos elementos deve manter geração dos demais grafos.
- Projeto com interfaces sem implementações não deve quebrar G5 e deve gerar grafo vazio válido.
- Método com cadeia de decisões sem branch alternativo explícito deve manter estrutura de arestas
  consistente para os branches existentes.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST carregar todos os arquivos JSON de um diretório de projeto e reconstruir
  os elementos de AST necessários para geração de grafos.
- **FR-002**: O sistema MUST gerar um arquivo DOT para cada um dos grafos G1 a G6 no diretório
  `graphs/` do projeto analisado.
- **FR-003**: O sistema MUST gerar arquivos DOT individuais de G7 em `graphs/g7_cfg/` apenas para
  métodos que contenham pelo menos um ponto de decisão de fluxo.
- **FR-004**: O grafo G1 MUST criar dependências entre tipos do projeto a partir de campo, parâmetro,
  retorno, chamada e instanciação, registrando a origem da dependência na aresta.
- **FR-005**: O grafo G2 MUST representar relações de herança e implementação com diferenciação do
  tipo de relação em cada aresta.
- **FR-006**: O grafo G3 MUST representar chamadas entre métodos do projeto, incluindo construtores.
- **FR-007**: O grafo G4 MUST conectar métodos e atributos por acesso com diferenciação entre leitura
  e escrita, e MUST gerar também uma projeção método–método baseada em atributo compartilhado.
- **FR-008**: O grafo G5 MUST representar interfaces e classes com estilos de nó distintos para
  facilitar leitura visual.
- **FR-009**: O grafo G6 MUST conectar classes clientes a interfaces quando a interface for usada em
  tipos de campo, tipos de parâmetro ou chamadas de método.
- **FR-010**: Todos os grafos MUST ignorar tipos primitivos, tipos de biblioteca padrão (incluindo
  `superclass` / interfaces JDK como `java.lang.Object`) e acesso de saída padrão, mantendo foco apenas
  em dependências internas do projeto onde aplicável.
- **FR-011**: O sistema MUST criar automaticamente os diretórios de saída de grafos quando não
  existirem.
- **FR-012**: A suíte de testes MUST validar, no mínimo, as regras críticas definidas para G1, G2,
  G3, G4, G5, G6 e G7 construindo modelos em memória e **sem ler ficheiros JSON de fixtures
  versionados** no repositório. **MAY** usar diretórios temporários (ex.: JUnit `@TempDir`) apenas
  para escrever e reler `.dot` gerados pelo código sob teste ou para simular um diretório de projeto
  vazio, desde que não dependa de JSON externo fixo.

### Key Entities *(include if feature involves data)*

- **Projeto Analisado**: Unidade de entrada contendo múltiplos artefatos de AST e um diretório de
  saída de grafos.
- **Artefato AST**: Representação de classes, interfaces, métodos, campos, chamadas, acessos e
  estruturas de controle extraída previamente.
- **Definição de Grafo**: Conjunto de regras que transforma artefatos AST em nós e arestas para um
  tipo específico de visualização.
- **Arquivo DOT**: Artefato textual final que descreve um grafo e pode ser renderizado por ferramentas
  de visualização.

### Assumptions

- Os JSONs de entrada seguem o contrato definido na etapa anterior e representam um único projeto por
  diretório.
- Nomes de tipos nos artefatos são suficientes para resolver relações entre elementos do próprio
  projeto sem dependência de compilação.
- A geração de grafos deve ser determinística: mesmas entradas produzem mesmos conteúdos DOT.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% das execuções com diretório válido contendo ASTs, os sete conjuntos de grafos
  esperados são produzidos nos caminhos de saída definidos para o projeto.
- **SC-002**: Em 100% dos cenários de teste definidos para filtros de ruído, elementos externos ao
  domínio do projeto não aparecem nos grafos gerados.
- **SC-003**: Em 100% dos testes de relações estruturais, as arestas obrigatórias para herança,
  implementação, chamadas, acessos e projeções são identificadas corretamente.
- **SC-004**: Em 100% dos testes de CFG, métodos sem pontos de decisão não produzem artefato e
  métodos com decisão produzem pelo menos um grafo válido.

# Feature Specification: Parser AST (etapa 1)

**Feature Branch**: `001-ast-parser`  
**Created**: 2026-03-20  
**Last updated**: 2026-03-20  
**Status**: Draft  
**Input**: User description: "ETAPA 1 — PARSER DA AST: módulo de parsing para análise estática Java (diretório → .java recursivos → artefatos JSON estruturados em output/)."

**Constitution**: Feature branch naming, testing, and layout MUST align with
`.specify/memory/constitution.md` (e.g. `NNN-kebab-case`, JUnit 5, Maven `src/main` / `src/test`).

**Audience**: Mantenedores e desenvolvedores da ferramenta de análise estática; linguagem
orientada a resultados observáveis (CLI, arquivos de saída, contagens).

## Motor de parsing *(escopo explícito)*

Esta etapa **não** inclui implementar lexer, parser ou gramática Java próprios. O parsing sintático,
a AST e a base para análise estrutural **MUST** ser obtidos com a biblioteca de código aberto
**JavaParser** ([repositório](https://github.com/javaparser/javaparser), [documentação](https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html)),
**declarado como dependência Maven** (`pom.xml`) e resolvido para o classpath do artefacto
executável—não há “API” HTTP nem serviço externo obrigatório. O consumo normal é: dependências no
build + chamadas, no código deste projeto, às **classes públicas da biblioteca** (API
programática Java: parse, `CompilationUnit`, *symbol solver*, etc.). O código aqui MUST atuar como
**integração e orquestração** (wrapping)—descoberta de ficheiros, invocação dessas classes,
configuração do *symbol solver* para o diretório raiz, travessia da AST e produção do JSON
contratado. Qualquer serialização JSON pode combinar utilitários do ecossistema JavaParser com um
modelo de saída próprio, desde que o parsing em si permaneça delegado ao JavaParser.

**Fora de escopo**: geradores de AST alternativos feitos “do zero” ou substituição do JavaParser
por outro motor nesta iteração (mudança exigiria novo spec/plano).

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Escanear um projeto e gerar artefatos por arquivo (Priority: P1)

Um desenvolvedor aponta a ferramenta para a raiz absoluta de um projeto fonte. A ferramenta
descobre todos os arquivos-fonte relevantes em qualquer profundidade, processa cada um e grava
um artefato estruturado por arquivo no diretório de saída do repositório, com nome derivado do
arquivo fonte e desambiguação se necessário.

**Why this priority**: Sem este fluxo não há insumo para etapas seguintes da análise.

**Independent Test**: Executar a ferramenta contra um diretório de exemplo com alguns arquivos
válidos e verificar que o número de artefatos gerados corresponde ao número de arquivos
processados com sucesso e que cada artefato tem conteúdo não vazio.

**Acceptance Scenarios**:

1. **Given** um diretório absoluto contendo arquivos `.java` em subpastas, **When** a ferramenta
   é executada com esse caminho, **Then** cada arquivo `.java` encontrado gera exatamente um
   artefato correspondente no diretório de saída, com conteúdo persistido.
2. **Given** um arquivo `.java` com uma classe simples, **When** o arquivo é processado com
   sucesso, **Then** o artefato inclui o nome da classe ou interface declarada de forma
   identificável.

---

### User Story 2 - Extrair estrutura para análise posterior (Priority: P2)

O analista precisa que cada artefato descreva elementos mínimos do tipo compilável: se é
abstrato, superclasse (se houver), interfaces implementadas, campos (nome e tipo), métodos (nome,
retorno, parâmetros) e, para cada método, as chamadas de método presentes no corpo.

**Why this priority**: Define o contrato de dados entre esta etapa e os analisadores SOLID
posteriores.

**Independent Test**: Alimentar a ferramenta com fonte embutido em cenário de teste (sem pastas
externas) contendo herança, interface e corpo com chamada; validar que o artefato serializado
reflete esses elementos.

**Acceptance Scenarios**:

1. **Given** um tipo que estende outro e implementa interfaces, **When** processado com sucesso,
   **Then** o artefato registra superclasse e interfaces de forma inspecionável.
2. **Given** um tipo com campos e métodos, **When** processado com sucesso, **Then** o artefato
   lista campos com nome e tipo e métodos com nome, tipo de retorno e parâmetros.
3. **Given** um método cujo corpo contém uma chamada a outro método, **When** processado com
   sucesso, **Then** essa chamada aparece associada ao método de origem no artefato.

---

### User Story 3 - Resiliência e resumo da execução (Priority: P3)

Durante varreduras reais, alguns arquivos podem ser inválidos ou rejeitados pelo **JavaParser** ao
tentar produzir a AST. O operador precisa que a execução não pare no primeiro erro, que falhas
sejam comunicadas de forma legível na saída padrão e que ao final apareça um resumo com quantidades
de sucesso e falha.

**Why this priority**: Permite processar bases reais sem intervenção manual arquivo a arquivo.

**Independent Test**: Executar sobre um conjunto misto com arquivo sintaticamente inválido;
verificar que a execução termina, que o arquivo inválido é relatado, que contadores finais batem
com a expectativa e que não há exceção não tratada que encerre o processo.

**Acceptance Scenarios**:

1. **Given** um arquivo `.java` com sintaxe inválida entre arquivos válidos, **When** a
   ferramenta roda, **Then** o nome (ou identificação acordada) do arquivo problemático é emitido
   na saída padrão, os demais arquivos seguem sendo processados e a execução completa normalmente.
2. **Given** uma execução que processou vários arquivos, **When** termina, **Then** a saída padrão
   inclui totais de arquivos processados com sucesso e de falhas.

---

### User Story 4 - Resolver tipos no contexto do projeto (Priority: P2)

Para chamadas e tipos referenciados dentro do mesmo conjunto fonte sob a raiz informada, o
resultado deve refletir resolução de tipos coerente com o projeto escaneado (tipos definidos no
próprio diretório raiz fornecido).

**Why this priority**: Reduz ruído nas etapas que dependem de tipagem e chamadas.

**Independent Test**: Projeto mínimo com dois tipos referenciados entre si sob a mesma raiz;
verificar que informação de tipo/call no artefato é consistente com essa estrutura.

**Acceptance Scenarios**:

1. **Given** tipos no mesmo projeto referenciados entre si, **When** a raiz de escaneamento é o
   diretório raiz desse projeto, **Then** a resolução de tipos usada na extração considera tipos
   encontrados sob essa raiz.

---

### Edge Cases

- Diretório vazio ou sem arquivos `.java`: execução termina com resumo coerente (zero sucessos ou
  mensagem clara).
- Múltiplos arquivos com o mesmo nome base em pastas diferentes: artefatos MUST ser distintos
  (por exemplo, codificando caminho relativo ao diretório de escaneamento no nome ou em
  subestrutura sob o diretório de saída).
- Caminho não absoluto ou inexistente: comportamento definido (erro claro na saída padrão e
  código de saída não zero, ou documentado explicitamente na entrega).
- Arquivos não-`.java` na árvore: ignorados silenciosamente.
- Limite de tamanho ou encoding: assumir UTF-8 salvo indicação contrária na implementação
  (documentar na fase de plano se outra política for necessária).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-000**: A ferramenta MUST integrar o **JavaParser** como **dependência Maven** (artefactos do
  ecossistema JavaParser no `pom.xml`, incluindo *symbol solving*) e MUST obter a AST e suportar a
  extração de tipos e chamadas **chamando o código da biblioteca no mesmo processo** (API
  programática Java, não serviço remoto); MUST NOT substituir esse papel por um parser sintático
  implementado neste repositório. O valor entregue nesta etapa é a **camada de produto** em volta do
  JavaParser (CLI, I/O, configuração do *solver*, mapeamento para o JSON acordado), não um
  concorrente do JavaParser.
- **FR-001**: A ferramenta MUST aceitar exatamente um argumento de linha de comando: caminho
  absoluto do diretório raiz a escanear.
- **FR-002**: A ferramenta MUST percorrer o diretório recursivamente e considerar apenas arquivos
  com sufixo `.java`.
- **FR-003**: Para cada arquivo `.java` elegível, a ferramenta MUST analisar sintática e
  estruturalmente o conteúdo **com o JavaParser carregado como dependência** (invocando a
  biblioteca no código da aplicação) e produzir um artefato serializado em formato
  JSON no diretório de saída do projeto (`output/` na raiz do repositório da ferramenta), salvo
  política de caminho documentada na fase de plano se o executável puder rodar com CWD diferente.
- **FR-004**: O nome de cada artefato MUST incorporar o nome base do arquivo `.java` e MUST ser
  **único por ficheiro fonte**; em colisão de basename entre pastas, MUST haver desambiguação
  (ex.: codificar caminho relativo à raiz de escaneamento no nome do ficheiro — ver Edge Cases).
- **FR-005**: Cada artefato JSON MUST incluir, para o tipo principal declarado no arquivo: nome da
  classe ou interface; indicador de abstrato; superclasse quando houver; lista de interfaces
  implementadas; campos com nome e tipo; métodos com nome, tipo de retorno e parâmetros; e, por
  método, as chamadas de método identificáveis presentes no corpo.
- **FR-006**: A ferramenta MUST configurar a resolução de tipos **com o *symbol solver* do
  JavaParser** (por exemplo `CombinedTypeSolver` apontando para o diretório raiz recebido e fontes
  necessárias), de modo que referências a tipos definidos sob essa raiz possam ser resolvidas
  durante a extração.
- **FR-007**: Se o processamento de um arquivo falhar, a ferramenta MUST registrar o problema na
  saída padrão (identificando o arquivo) e MUST continuar com os demais arquivos.
- **FR-008**: Ao término, a ferramenta MUST imprimir na saída padrão o número de arquivos
  processados com sucesso e o número de falhas.
- **FR-009**: A suíte de testes automatizados MUST validar, sem dependência de diretórios externos
  nem rede (por exemplo usando `StaticJavaParser` ou equivalente da API sobre *strings* de código
  de teste): extração de nome de classe simples; presença de campos e métodos; herança e
  interfaces; arquivo inválido que não aborta a execução e incrementa falhas; criação de artefato
  JSON não vazio em `output/` em cenário controlado.

### Key Entities

- **Artefato de unidade de compilação**: Representação serializada de um arquivo `.java` cuja AST
  foi obtida com JavaParser; agrega metadados do tipo principal e membros, mais chamadas por
  método.
- **Membro (campo ou método)**: Nome, assinatura de tipo visível, e para método lista de chamadas
  inferidas do corpo.
- **Resultado de execução do scanner**: Contagens agregadas de sucesso e falha e log textual de
  falhas por arquivo.

### Assumptions and delivery constraints *(iteration 1 — planning input)*

- Empacotamento Maven (`groupId` `com.solidanalysis`, `artifactId` `solid-static-analysis`), Java
  17 como nível de linguagem, código do scanner sob `com.solidanalysis.scanner`, testes espelhados
  em `src/test/java`.
- Dependências obrigatórias da iteração: artefactos **JavaParser** em Maven Central — no mínimo
  núcleo de parsing (`javaparser-core`) e **symbol solver** (`javaparser-symbol-solver-core` ou
  conjunto equivalente recomendado pelo projeto JavaParser); mais **JUnit 5** para testes.
  Versões fixadas no `pom.xml` no plano de implementação.
- O *symbol solver* do JavaParser tem limitações conhecidas (ex.: generics em alguns cantos); o
  plano MAY documentar o que fazer quando a resolução falhar (representação explícita no JSON vs.
  valor desconhecido), sem abandonar o JavaParser.
- Invocação de exemplo: executável empacotado (`java -jar …`) recebendo o caminho absoluto da raiz
  do projeto a analisar.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para um conjunto de referência com `N` arquivos `.java` sintaticamente válidos e
  distintos na árvore, uma única execução produz `N` artefatos JSON distintos no diretório de
  saída, cada um com tamanho maior que zero.
- **SC-002**: Em execução com arquivos válidos e pelo menos um arquivo inválido, o processo
  completa sem interrupção por exceção não tratada e o resumo final reporta pelo menos uma falha e
  pelo menos um sucesso coerente com o conjunto de entrada.
- **SC-003**: Em inspeção automatizada (teste), um tipo com superclasse, interface implementada,
  campos, métodos e uma chamada no corpo de método gera artefato onde cada um desses elementos é
  verificável por conteúdo.
- **SC-004**: O operador humano consegue, em uma execução manual contra um projeto Java local
  arbitrário, localizar em `output/` os artefatos esperados (um por arquivo fonte processado com
  sucesso) sem etapas não documentadas.

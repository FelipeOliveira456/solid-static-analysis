# Quickstart: Graph Algorithms Analysis (Etapa 3)

## Build

```bash
mvn clean package
```

O artefato empacotado deve ser o jar sombreado (shade) em `target/solid-static-analysis.jar`.

## Executar analise (modo Etapa 3)

1. Garanta que a Etapa 2 ja foi executada e que existe `output/<projeto>/graphs/` com os `.dot` gerados.
2. Rode:

```bash
java -jar target/solid-static-analysis.jar --analyze /caminho/para/output/meu-projeto
```

Ao final, os resultados devem aparecer em `output/<projeto>/algorithms/`, com arquivos JSON por grafo/classe/metodo seguindo as convencoes da especificacao.

## Rodar testes

```bash
mvn test
```


# Quickstart: 001-ast-parser

**Repo root**: `/home/felipe/Documents/AS/solid-static-analysis` (ajustar à tua máquina)

## Pré-requisitos

- JDK **17+**
- Maven **3.9+**

## Build

```bash
cd /home/felipe/Documents/AS/solid-static-analysis
mvn -q clean package
```

O JAR sombreado esperado (nome exato conforme `pom.xml`): `target/solid-static-analysis.jar` ou
artefacto definido no shade — verificar após primeira build.

## Executar o scanner

A partir da **raiz do repositório** da ferramenta (para que `output/` seja criado ao lado de
`pom.xml`):

```bash
cd /home/felipe/Documents/AS/solid-static-analysis
java -jar target/solid-static-analysis.jar /caminho/absoluto/para/projeto-java
```

Substituir o segundo argumento por um diretório absoluto real contendo `.java`.

## Testes

```bash
mvn -q test
```

## Onde ver resultados

- Artefatos JSON: diretório `output/` na raiz do repositório (CWD).
- Stdout: falhas por ficheiro + linha de totais.

## Documentação da feature

- Especificação: [spec.md](./spec.md)
- Plano: [plan.md](./plan.md)
- Contrato JSON: [contracts/ast-artifact.schema.json](./contracts/ast-artifact.schema.json)
- CLI: [contracts/cli.md](./contracts/cli.md)

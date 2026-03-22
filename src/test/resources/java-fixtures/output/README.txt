Snapshot de referência: ast/ (JSON do scan), graphs/, algorithms/, scoring/ e results/ (Etapas 1–5).
O `relaxFactor` nos JSON segue `scoring.relax.k` em `analysis.properties` na raiz do repo (valor atual: ver essa chave; o snapshot foi gerado com o repo em `user.dir` para o merge de propriedades).

Regenerar a partir dos *.java em ../ (na raiz do repositório solid-static-analysis, Louvain por defeito):

  mvn -q package
  java -jar target/solid-static-analysis.jar --all "$(pwd)/src/test/resources/java-fixtures" \
    --output "$(pwd)/src/test/resources/java-fixtures/output"

Para algoritmos sem Louvain (só métricas):

  java -jar target/solid-static-analysis.jar --analyze "$(pwd)/src/test/resources/java-fixtures/output" --no-clustering

Os testes JUnit geram artefactos em target/java-fixtures-pipeline-cache/ (ou @TempDir) quando
aplicável.
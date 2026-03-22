Este diretório fica vazio no repositório (apenas este README).

Para regenerar scan + graphs + algorithms + scoring a partir dos *.java em ../ :

  cd <raiz do solid-static-analysis>
  mvn -q package
  java -jar target/solid-static-analysis.jar --all "$(pwd)/src/test/resources/java-fixtures" \
    --output "$(pwd)/src/test/resources/java-fixtures/output" --clustering

Os testes JUnit geram artefactos em target/java-fixtures-pipeline-cache/ (ou @TempDir) e não
dependem de ficheiros commitados aqui.

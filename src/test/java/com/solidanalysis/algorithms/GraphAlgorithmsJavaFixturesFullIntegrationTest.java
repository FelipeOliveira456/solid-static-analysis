package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import com.solidanalysis.algorithms.runners.GraphAnalyzeOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Full {@code java-fixtures/output/graphs} tree without Louvain clustering (default CLI speed). Layout:
 * {@code output/&lt;projeto&gt;/graphs} → {@code output/&lt;projeto&gt;/algorithms/}.
 */
class GraphAlgorithmsJavaFixturesFullIntegrationTest {

    private static final int MIN_EXPECTED_JSON_FILES = 31;

    @Test
    void fullJavaFixturesWithoutClusteringProducesParseableJson(@TempDir Path tmp) throws Exception {
        Path projectOut = tmp.resolve("output/java-fixtures-full");
        Path graphs = projectOut.resolve("graphs");
        Files.createDirectories(graphs);
        GraphTestFixtures.copyAllFixtureGraphs(GraphTestFixtures.fixtureGraphsDir(), graphs);

        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        assertTrue(
                new GraphAlgorithmsRunner()
                        .run(projectOut, h, GraphAnalyzeOptions.defaults()),
                "runner failed; warnings: " + String.join("; ", h.getWarnings()));

        Path algorithms = projectOut.resolve("algorithms");
        assertTrue(Files.isDirectory(algorithms));

        ObjectMapper mapper = new ObjectMapper();
        AtomicInteger count = new AtomicInteger();
        try (Stream<Path> walk = Files.walk(algorithms)) {
            walk.filter(p -> p.toString().endsWith(".json"))
                    .forEach(
                            p -> {
                                try {
                                    mapper.readTree(p.toFile());
                                    count.incrementAndGet();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        }
        assertTrue(
                count.get() >= MIN_EXPECTED_JSON_FILES,
                "expected at least " + MIN_EXPECTED_JSON_FILES + " JSON files, got " + count.get());
    }
}

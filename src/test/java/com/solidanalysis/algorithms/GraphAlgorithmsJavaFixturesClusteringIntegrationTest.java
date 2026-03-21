package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import com.solidanalysis.algorithms.runners.GraphAnalyzeOptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Full {@code java-fixtures} graph tree with Louvain clustering enabled. Uses production layout under the
 * build directory:
 *
 * <pre>
 *   target/java-fixtures-clustering-output/output/java-fixtures-demo/graphs/
 *   target/java-fixtures-clustering-output/output/java-fixtures-demo/algorithms/
 * </pre>
 *
 * <p>Tagged {@code clustering}. Optional second constructor arg is legacy and ignored (Louvain
 * discovers {@code k}).
 */
@Tag("clustering")
class GraphAlgorithmsJavaFixturesClusteringIntegrationTest {

    private static final int MIN_EXPECTED_JSON_FILES = 31;

    @Test
    @Timeout(value = 15, unit = TimeUnit.MINUTES)
    void fullJavaFixturesWithClusteringProducesValidJsonUnderOutputAlgorithmsLayout() throws Exception {
        Path projectOut =
                Path.of("target/java-fixtures-clustering-output/output/java-fixtures-demo")
                        .toAbsolutePath();
        deleteRecursivelyIfExists(projectOut);
        Path graphs = projectOut.resolve("graphs");
        Files.createDirectories(graphs);
        GraphTestFixtures.copyAllFixtureGraphs(GraphTestFixtures.fixtureGraphsDir(), graphs);

        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        boolean ok =
                new GraphAlgorithmsRunner()
                        .run(projectOut, h, new GraphAnalyzeOptions(true, 2));
        assertTrue(ok, "runner failed; warnings: " + String.join("; ", h.getWarnings()));

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

        JsonNode g1 = mapper.readTree(algorithms.resolve("g1_algorithms.json").toFile());
        assertTrue(g1.get("clusters").isArray());
        assertTrue(
                g1.get("clusters").size() >= 1,
                "G1 with clustering should contain at least one Louvain community");

        JsonNode g3ContaBancaria =
                mapper.readTree(algorithms.resolve("g3_algorithms/ContaBancaria.json").toFile());
        Set<String> g3Nodes = jsonObjectFieldNames(g3ContaBancaria.get("inDegree"));
        Set<String> g3ClusteredNodes = nodesFromClusterArray(g3ContaBancaria.get("clusters"));
        assertTrue(
                g3ClusteredNodes.containsAll(g3Nodes),
                "G3 ContaBancaria clusters must include all methods (including isolated nodes)");
    }

    private static void deleteRecursivelyIfExists(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(root)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(
                            p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        }
    }

    private static Set<String> jsonObjectFieldNames(JsonNode obj) {
        Set<String> out = new HashSet<>();
        Iterator<String> it = obj.fieldNames();
        while (it.hasNext()) {
            out.add(it.next());
        }
        return out;
    }

    private static Set<String> nodesFromClusterArray(JsonNode clusters) {
        Set<String> out = new HashSet<>();
        for (JsonNode cluster : clusters) {
            for (JsonNode node : cluster) {
                out.add(node.asText());
            }
        }
        return out;
    }
}

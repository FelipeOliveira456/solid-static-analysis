package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import com.solidanalysis.algorithms.runners.GraphAnalyzeOptions;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Louvain via runner on a tiny synthetic G1 (fast). */
class GraphAlgorithmsRunnerClusteringTest {

    @Test
    void clusteringPopulatesG1WhenEnabled(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        Files.writeString(
                graphs.resolve("g1_dependency.dot"),
                """
                digraph G1_tiny {
                  a1 -> a2;
                  a2 -> a3;
                  a3 -> a1;
                  b1 -> b2;
                  b2 -> b3;
                  b3 -> b1;
                  a1 -> b1;
                }
                """);
        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        assertTrue(new GraphAlgorithmsRunner().run(tmp, h, new GraphAnalyzeOptions(true)));
        var tree =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(tmp.resolve("algorithms/g1_algorithms.json").toFile());
        assertTrue(tree.get("clusters").isArray());
        assertTrue(tree.get("clusters").size() >= 2);
        Set<String> fromClusters = nodesFromClusterArray(tree.get("clusters"));
        assertEquals(Set.of("a1", "a2", "a3", "b1", "b2", "b3"), fromClusters);
    }

    @Test
    void clusteringOffLeavesEmptyClustersArray(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        Files.writeString(
                graphs.resolve("g1_dependency.dot"),
                "digraph G { A -> B; }\n");
        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        assertTrue(new GraphAlgorithmsRunner().run(tmp, h, GraphAnalyzeOptions.defaults()));
        var tree =
                new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(tmp.resolve("algorithms/g1_algorithms.json").toFile());
        assertEquals(0, tree.get("clusters").size());
    }

    private static Set<String> nodesFromClusterArray(com.fasterxml.jackson.databind.JsonNode clusters) {
        Set<String> out = new HashSet<>();
        for (com.fasterxml.jackson.databind.JsonNode cluster : clusters) {
            for (com.fasterxml.jackson.databind.JsonNode node : cluster) {
                out.add(node.asText());
            }
        }
        return out;
    }
}

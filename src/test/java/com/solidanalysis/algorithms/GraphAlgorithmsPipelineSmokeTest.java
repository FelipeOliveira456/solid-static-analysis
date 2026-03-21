package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.SolidAnalysisCli;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphAlgorithmsPipelineSmokeTest {

    @Test
    void analyzeCliProducesJson(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        GraphTestFixtures.copyMinimalFixtureGraphs(GraphTestFixtures.fixtureGraphsDir(), graphs);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int ok =
                SolidAnalysisCli.run(
                        new String[] {"--analyze", tmp.toAbsolutePath().toString()},
                        new PrintStream(bout),
                        new PrintStream(berr));
        assertEquals(SolidAnalysisCli.EXIT_OK, ok, berr.toString(StandardCharsets.UTF_8));
        ObjectMapper mapper = new ObjectMapper();
        AtomicInteger n = new AtomicInteger();
        try (Stream<Path> walk = Files.walk(tmp.resolve("algorithms"))) {
            walk.filter(p -> p.toString().endsWith(".json"))
                    .forEach(
                            p -> {
                                try {
                                    mapper.readTree(p.toFile());
                                    n.incrementAndGet();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        }
        assertTrue(n.get() >= 8);
    }

    @Test
    void analyzeRejectsNonAbsolutePath() {
        int code =
                SolidAnalysisCli.run(
                        new String[] {"--analyze", "relative/out"},
                        new PrintStream(new ByteArrayOutputStream()),
                        new PrintStream(new ByteArrayOutputStream()));
        assertEquals(SolidAnalysisCli.EXIT_ERROR, code);
    }

    @Test
    void analyzeWithClusteringFlagRunsOnSmallG1(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        String tinyG1 =
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
                """;
        Files.writeString(graphs.resolve("g1_dependency.dot"), tinyG1);
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {
                            "--analyze",
                            tmp.toAbsolutePath().toString(),
                            "--clustering"
                        },
                        new PrintStream(new ByteArrayOutputStream()),
                        new PrintStream(berr));
        assertEquals(SolidAnalysisCli.EXIT_OK, code, berr.toString(StandardCharsets.UTF_8));
        var root =
                new ObjectMapper()
                        .readTree(tmp.resolve("algorithms/g1_algorithms.json").toFile());
        assertTrue(root.get("clusters").isArray());
        assertTrue(root.get("clusters").size() >= 2);
    }
}

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

class GraphAlgorithmsIntegrationFixturesTest {

    @Test
    void fixtureGraphsProduceParseableJsonUnderAlgorithms(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        GraphTestFixtures.copyMinimalFixtureGraphs(GraphTestFixtures.fixtureGraphsDir(), graphs);
        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        assertTrue(
                new GraphAlgorithmsRunner()
                        .run(tmp, h, GraphAnalyzeOptions.defaults()));
        Path algorithms = tmp.resolve("algorithms");
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
        assertTrue(count.get() >= 8, "expected core JSON outputs, got " + count.get());
    }
}

package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G6InterfaceUsageAnalyzerTest {

    @Test
    void g6UsesInterfacesFromG5Fixture(@TempDir Path tmp) throws Exception {
        Path graphs = tmp.resolve("graphs");
        Files.createDirectories(graphs);
        Path fixtureBase = fixtureGraphsDir();
        Files.copy(
                fixtureBase.resolve("g5_interface_impl.dot"),
                graphs.resolve("g5_interface_impl.dot"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.copy(
                fixtureBase.resolve("g6_interface_usage.dot"),
                graphs.resolve("g6_interface_usage.dot"),
                StandardCopyOption.REPLACE_EXISTING);
        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        assertEquals(true, new GraphAlgorithmsRunner().run(tmp, h));
        JsonNode root =
                new ObjectMapper()
                        .readTree(tmp.resolve("algorithms/g6_algorithms.json").toFile());
        assertEquals(1, root.get("interfaceInDegree").get("Tributavel").asInt());
        assertNotNull(root.get("knownInterfacesFromG5"));
    }

    private static Path fixtureGraphsDir() throws Exception {
        var url =
                Objects.requireNonNull(
                        G6InterfaceUsageAnalyzerTest.class.getResource(
                                "/java-fixtures/output/graphs/g5_interface_impl.dot"));
        return Path.of(url.toURI()).getParent();
    }
}

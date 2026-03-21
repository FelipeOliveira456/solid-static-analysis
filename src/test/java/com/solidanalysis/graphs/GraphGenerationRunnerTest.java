package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphGenerationRunnerTest {

    @Test
    void generatesExpectedDotFiles(@TempDir Path tmp) throws Exception {
        String json =
                "{\"sourceFile\":\"/x.java\",\"primaryType\":{\"kind\":\"class\",\"name\":\"X\","
                        + "\"abstract\":false,\"superclass\":null,\"implementedInterfaces\":[],"
                        + "\"fields\":[],\"methods\":[]}}";
        Files.writeString(tmp.resolve("x.json"), json, StandardCharsets.UTF_8);
        new GraphGenerationRunner().run(tmp);
        Path graphs = tmp.resolve("graphs");
        assertTrue(Files.exists(graphs.resolve("g1_dependency.dot")));
        assertTrue(Files.exists(graphs.resolve("g2_inheritance.dot")));
        assertTrue(Files.isDirectory(graphs.resolve("g3_method_calls")));
        assertTrue(Files.exists(graphs.resolve("g3_method_calls").resolve("X.dot")));
        assertTrue(Files.isDirectory(graphs.resolve("g4_field_usage")));
        assertTrue(Files.exists(graphs.resolve("g4_field_usage").resolve("X.dot")));
        assertTrue(Files.isDirectory(graphs.resolve("g4_method_projection")));
        assertTrue(Files.exists(graphs.resolve("g4_method_projection").resolve("X.dot")));
        assertTrue(Files.exists(graphs.resolve("g5_interface_impl.dot")));
        assertTrue(Files.exists(graphs.resolve("g6_interface_usage.dot")));
        assertTrue(Files.isDirectory(graphs.resolve("g7_cfg")));
    }

    @Test
    void emptyJsonDirectoryStillWritesValidGraphs(@TempDir Path tmp) throws Exception {
        new GraphGenerationRunner().run(tmp);
        Path graphs = tmp.resolve("graphs");
        assertTrue(Files.exists(graphs.resolve("g1_dependency.dot")));
        String g1 = Files.readString(graphs.resolve("g1_dependency.dot"), StandardCharsets.UTF_8);
        assertTrue(g1.contains("digraph"));
    }
}

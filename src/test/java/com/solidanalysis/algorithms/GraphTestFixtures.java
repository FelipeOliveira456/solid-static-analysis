package com.solidanalysis.algorithms;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

/** Shared test resources: small graph slice for fast pipeline tests (no heavy clustering). */
final class GraphTestFixtures {

    private GraphTestFixtures() {}

    static Path fixtureGraphsDir() throws Exception {
        var url =
                Objects.requireNonNull(
                        GraphTestFixtures.class.getResource(
                                "/java-fixtures/output/graphs/g1_dependency.dot"));
        return Path.of(url.toURI()).getParent();
    }

    /**
     * Copies a minimal subset so {@link com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner}
     * finishes quickly without Louvain clustering.
     */
    static void copyMinimalFixtureGraphs(Path sourceGraphs, Path destGraphs) throws IOException {
        copy(sourceGraphs.resolve("g1_dependency.dot"), destGraphs.resolve("g1_dependency.dot"));
        copy(sourceGraphs.resolve("g2_inheritance.dot"), destGraphs.resolve("g2_inheritance.dot"));
        copy(sourceGraphs.resolve("g5_interface_impl.dot"), destGraphs.resolve("g5_interface_impl.dot"));
        copy(sourceGraphs.resolve("g6_interface_usage.dot"), destGraphs.resolve("g6_interface_usage.dot"));
        Path g3 = destGraphs.resolve("g3_method_calls");
        Files.createDirectories(g3);
        copy(
                sourceGraphs.resolve("g3_method_calls/Tributavel.dot"),
                g3.resolve("Tributavel.dot"));
        Path g4f = destGraphs.resolve("g4_field_usage");
        Files.createDirectories(g4f);
        copy(
                sourceGraphs.resolve("g4_field_usage/Tributavel.dot"),
                g4f.resolve("Tributavel.dot"));
        Path g4p = destGraphs.resolve("g4_method_projection");
        Files.createDirectories(g4p);
        copy(
                sourceGraphs.resolve("g4_method_projection/Tributavel.dot"),
                g4p.resolve("Tributavel.dot"));
        Path g7 = destGraphs.resolve("g7_cfg");
        Files.createDirectories(g7);
        copy(
                sourceGraphs.resolve("g7_cfg/ContaCorrente_cobrarTaxa.dot"),
                g7.resolve("ContaCorrente_cobrarTaxa.dot"));
    }

    /** Copies the entire {@code graphs/} tree from Etapa 2 fixtures (full integration / clustering). */
    static void copyAllFixtureGraphs(Path sourceGraphsRoot, Path destGraphsRoot) throws IOException {
        try (Stream<Path> walk = Files.walk(sourceGraphsRoot)) {
            walk.filter(Files::isRegularFile)
                    .forEach(
                            p -> {
                                try {
                                    Path rel = sourceGraphsRoot.relativize(p);
                                    Path target = destGraphsRoot.resolve(rel);
                                    Files.createDirectories(target.getParent());
                                    Files.copy(p, target);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        }
    }

    private static void copy(Path from, Path to) throws IOException {
        Files.createDirectories(to.getParent());
        Files.copy(from, to);
    }
}

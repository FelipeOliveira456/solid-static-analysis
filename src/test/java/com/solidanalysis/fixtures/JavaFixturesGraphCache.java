package com.solidanalysis.fixtures;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Lazily materializes {@code java-fixtures} Etapa 1–3 output under {@code target/} so tests do not
 * depend on committed {@code java-fixtures/output/graphs}.
 */
public final class JavaFixturesGraphCache {

    private static final Object LOCK = new Object();
    private static volatile Path CACHE;

    private JavaFixturesGraphCache() {}

    /** Project output dir containing {@code graphs/}, {@code algorithms/}, root {@code *.json}. */
    public static Path cachedProjectOutput() throws Exception {
        Path c = CACHE;
        if (c != null && Files.isRegularFile(c.resolve("graphs/g1_dependency.dot"))) {
            return c;
        }
        synchronized (LOCK) {
            c = CACHE;
            if (c != null && Files.isRegularFile(c.resolve("graphs/g1_dependency.dot"))) {
                return c;
            }
            Path base =
                    Path.of("target/java-fixtures-pipeline-cache/project-output")
                            .toAbsolutePath()
                            .normalize();
            deleteRecursivelyIfExists(base);
            Files.createDirectories(base);
            Path repoRoot = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
            JavaFixturesPipeline.runThroughAnalyze(base, repoRoot);
            CACHE = base;
            return base;
        }
    }

    public static Path cachedGraphsRoot() throws Exception {
        return cachedProjectOutput().resolve("graphs");
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
}

package com.solidanalysis.algorithms.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Discovers graph DOT paths under {@code graphs/} matching Etapa 2 layout.
 */
public final class GraphFileMapper {

    private final Path graphsDir;

    public GraphFileMapper(Path projectOutputDir) {
        this.graphsDir = projectOutputDir.resolve("graphs");
    }

    public Path getGraphsDir() {
        return graphsDir;
    }

    public boolean graphsDirExists() {
        return Files.isDirectory(graphsDir);
    }

    public Path g1() {
        return graphsDir.resolve("g1_dependency.dot");
    }

    public Path g2() {
        return graphsDir.resolve("g2_inheritance.dot");
    }

    public Path g5() {
        return graphsDir.resolve("g5_interface_impl.dot");
    }

    public Path g6() {
        return graphsDir.resolve("g6_interface_usage.dot");
    }

    public Map<String, Path> listDotFilesInSubdir(String subdir) throws IOException {
        Path dir = graphsDir.resolve(subdir);
        Map<String, Path> map = new LinkedHashMap<>();
        if (!Files.isDirectory(dir)) {
            return map;
        }
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> p.getFileName().toString().endsWith(".dot"))
                    .sorted()
                    .forEach(
                            p -> {
                                String name = p.getFileName().toString();
                                String key = name.substring(0, name.length() - ".dot".length());
                                map.put(key, p);
                            });
        }
        return map;
    }
}

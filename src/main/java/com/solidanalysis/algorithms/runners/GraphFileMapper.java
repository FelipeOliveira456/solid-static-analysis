package com.solidanalysis.algorithms.runners;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Discovers graph DOT paths under {@code graphs/} matching Etapa 2 layout (flat or mirrored under
 * source-relative subfolders).
 */
public final class GraphFileMapper {

    /**
     * One per-class DOT under {@code graphs/[<mirror>/]<layerDir>/<stem>.dot}; {@code mirror} is
     * empty for the legacy flat layout.
     */
    public record MirroredDot(Path mirrorRelativeToGraphs, String fileStem, Path dotFile) {}

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

    /**
     * Lists {@code .dot} files whose parent directory is named {@code layerDirectoryName} (e.g.
     * {@code g3_method_calls}), anywhere under {@code graphs/}. {@code mirrorRelativeToGraphs} is
     * the path from {@code graphs/} to the parent of that layer folder.
     */
    public List<MirroredDot> listMirroredLayerDots(String layerDirectoryName) throws IOException {
        List<MirroredDot> out = new ArrayList<>();
        if (!Files.isDirectory(graphsDir)) {
            return out;
        }
        try (Stream<Path> walk = Files.walk(graphsDir)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".dot"))
                    .forEach(
                            p -> {
                                Path layerDir = p.getParent();
                                if (layerDir == null
                                        || !layerDirectoryName.equals(
                                                layerDir.getFileName().toString())) {
                                    return;
                                }
                                Path mirror = graphsDir.relativize(layerDir.getParent());
                                String fn = p.getFileName().toString();
                                String stem = fn.substring(0, fn.length() - ".dot".length());
                                out.add(new MirroredDot(mirror, stem, p));
                            });
        }
        out.sort(
                Comparator.comparing(MirroredDot::mirrorRelativeToGraphs)
                        .thenComparing(MirroredDot::fileStem));
        return out;
    }
}

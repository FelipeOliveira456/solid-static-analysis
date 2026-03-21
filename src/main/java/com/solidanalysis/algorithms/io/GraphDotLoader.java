package com.solidanalysis.algorithms.io;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTImporter;

/**
 * Loads Etapa 2 DOT files using JGraphT {@link DOTImporter} into multigraphs so parallel edges
 * (e.g. G1) are preserved for degree metrics.
 */
public final class GraphDotLoader {

    private GraphDotLoader() {}

    /**
     * @param ellipseShapeNodeIds if non-null, node ids with {@code shape=ellipse} are added (for
     *     G5 interface detection)
     */
    public static DirectedPseudograph<String, DefaultEdge> loadDirected(
            Path path, Set<String> ellipseShapeNodeIds) throws IOException {
        DirectedPseudograph<String, DefaultEdge> g = new DirectedPseudograph<>(DefaultEdge.class);
        importDot(path, g, ellipseShapeNodeIds);
        return g;
    }

    public static DirectedPseudograph<String, DefaultEdge> loadDirected(Path path)
            throws IOException {
        return loadDirected(path, null);
    }

    public static Multigraph<String, DefaultEdge> loadUndirected(Path path) throws IOException {
        Multigraph<String, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        importDot(path, g, null);
        return g;
    }

    private static <G extends org.jgrapht.Graph<String, DefaultEdge>> void importDot(
            Path path, G graph, Set<String> ellipseShapeNodeIds) throws IOException {
        DOTImporter<String, DefaultEdge> importer = new DOTImporter<>();
        importer.setVertexFactory(java.util.function.Function.identity());
        importer.setVertexWithAttributesFactory(
                (id, attrs) -> {
                    if (ellipseShapeNodeIds != null) {
                        Attribute shape = attrs.get("shape");
                        if (shape != null && "ellipse".equalsIgnoreCase(shape.getValue().trim())) {
                            ellipseShapeNodeIds.add(id);
                        }
                    }
                    return id;
                });
        importer.setEdgeWithAttributesFactory(attrs -> new DefaultEdge());
        try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            importer.importGraph(graph, r);
        }
    }

    /**
     * Merges interface ids from raw DOT text (fallback when {@link DOTImporter} attribute maps omit
     * {@code shape}).
     */
    public static void mergeEllipseShapeNodesFromDotFile(Path path, Set<String> ellipseShapeNodeIds)
            throws IOException {
        if (ellipseShapeNodeIds == null) {
            return;
        }
        for (String raw : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("//") || !line.contains("[")) {
                continue;
            }
            String compact = line.replaceAll("\\s+", "").toLowerCase();
            if (!compact.contains("shape=ellipse")) {
                continue;
            }
            int lb = line.indexOf('[');
            String head = line.substring(0, lb).trim();
            if (head.isEmpty()) {
                continue;
            }
            String id = head.split("\\s+")[0];
            ellipseShapeNodeIds.add(id);
        }
    }
}

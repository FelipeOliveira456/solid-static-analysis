package com.solidanalysis.algorithms.runners;

import com.solidanalysis.algorithms.algorithms.ConnectedComponentsAnalyzer;
import com.solidanalysis.algorithms.algorithms.DegreeCentralityAnalyzer;
import com.solidanalysis.algorithms.algorithms.DegreeMetricsAnalyzer;
import com.solidanalysis.algorithms.algorithms.G7CfgAnalyzer;
import com.solidanalysis.algorithms.algorithms.LouvainClusteringAnalyzer;
import com.solidanalysis.algorithms.algorithms.IsolatedNodesAnalyzer;
import com.solidanalysis.algorithms.algorithms.LcomAnalyzer;
import com.solidanalysis.algorithms.algorithms.LongestPathDagAnalyzer;
import com.solidanalysis.algorithms.algorithms.SccKosarajuAnalyzer;
import com.solidanalysis.algorithms.io.AlgorithmsJsonWriter;
import com.solidanalysis.algorithms.io.GraphDotLoader;
import com.solidanalysis.algorithms.model.G1AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G2AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G3AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4FieldAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4ProjectionAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G5AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G6AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G7AlgorithmsDocument;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedPseudograph;
import org.jgrapht.graph.Multigraph;
import org.jgrapht.graph.SimpleGraph;

/**
 * Orchestrates loading DOT graphs from {@code output/<projeto>/graphs/} and writing JSON under
 * {@code algorithms/}.
 */
public final class GraphAlgorithmsRunner {

    /**
     * Same as {@link #run(Path, GraphAlgorithmsErrorHandler, GraphAnalyzeOptions)} with {@link
     * GraphAnalyzeOptions#defaults()} (no Louvain clustering).
     */
    public boolean run(Path projectOutputDir, GraphAlgorithmsErrorHandler errors) {
        return run(projectOutputDir, errors, GraphAnalyzeOptions.defaults());
    }

    /**
     * @return {@code false} if {@code graphs/} is missing or an unrecoverable I/O error occurs
     */
    public boolean run(
            Path projectOutputDir,
            GraphAlgorithmsErrorHandler errors,
            GraphAnalyzeOptions options) {
        Path graphsDir = projectOutputDir.resolve("graphs");
        if (!Files.isDirectory(graphsDir)) {
            errors.warn("Missing graphs directory: " + graphsDir.toAbsolutePath());
            return false;
        }
        Path algorithmsDir = projectOutputDir.resolve("algorithms");
        AlgorithmsJsonWriter json = new AlgorithmsJsonWriter();
        GraphFileMapper mapper = new GraphFileMapper(projectOutputDir);
        boolean clustering = options.isGirvanNewmanClustering();
        try {
            List<String> g5Interfaces =
                    runG1G2G5G7(mapper, algorithmsDir, json, errors, clustering);
            runG3(mapper, algorithmsDir, json, errors, clustering);
            runG4Field(mapper, algorithmsDir, json, errors);
            runG4Projection(mapper, algorithmsDir, json, errors, clustering);
            runG6IfNeeded(mapper, algorithmsDir, json, errors, g5Interfaces);
        } catch (IOException e) {
            errors.warn("I/O error: " + e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Runs G1, G2, G5, G7; returns sorted interface ids from G5 (for G6), or empty if G5 skipped.
     */
    private List<String> runG1G2G5G7(
            GraphFileMapper mapper,
            Path algorithmsDir,
            AlgorithmsJsonWriter json,
            GraphAlgorithmsErrorHandler errors,
            boolean runClustering)
            throws IOException {
        List<String> g5Interfaces = List.of();
        Path g1 = mapper.g1();
        if (Files.isRegularFile(g1)) {
            try {
                DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(g1);
                G1AlgorithmsDocument doc = buildG1(g, runClustering);
                json.write(algorithmsDir.resolve("g1_algorithms.json"), doc);
            } catch (Exception e) {
                errors.warn("G1: failed to analyze " + g1 + ": " + e.getMessage());
            }
        } else {
            errors.warn("G1: missing file " + g1.toAbsolutePath());
        }

        Path g2 = mapper.g2();
        if (Files.isRegularFile(g2)) {
            try {
                DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(g2);
                G2AlgorithmsDocument doc = buildG2(g);
                json.write(algorithmsDir.resolve("g2_algorithms.json"), doc);
            } catch (Exception e) {
                errors.warn("G2: failed to analyze " + g2 + ": " + e.getMessage());
            }
        } else {
            errors.warn("G2: missing file " + g2.toAbsolutePath());
        }

        Path g5 = mapper.g5();
        if (Files.isRegularFile(g5)) {
            try {
                DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(g5);
                Set<String> ellipse = new LinkedHashSet<>();
                GraphDotLoader.mergeEllipseShapeNodesFromDotFile(g5, ellipse);
                G5AlgorithmsDocument doc = buildG5(g, ellipse);
                json.write(algorithmsDir.resolve("g5_algorithms.json"), doc);
                g5Interfaces = new ArrayList<>(ellipse);
                g5Interfaces.sort(String::compareTo);
            } catch (Exception e) {
                errors.warn("G5: failed to analyze " + g5 + ": " + e.getMessage());
            }
        } else {
            errors.warn("G5: missing file " + g5.toAbsolutePath());
        }

        List<GraphFileMapper.MirroredDot> g7dots = mapper.listMirroredLayerDots("g7_cfg");
        if (g7dots.isEmpty()) {
            errors.warn("G7: no .dot files under any .../g7_cfg/ directory");
        } else {
            for (GraphFileMapper.MirroredDot md : g7dots) {
                Path dot = md.dotFile();
                try {
                    DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(dot);
                    G7AlgorithmsDocument doc = G7CfgAnalyzer.analyze(g);
                    Path outDir =
                            algorithmsDir
                                    .resolve(md.mirrorRelativeToGraphs())
                                    .resolve("g7_algorithms");
                    Files.createDirectories(outDir);
                    json.write(outDir.resolve(md.fileStem() + ".json"), doc);
                } catch (Exception e) {
                    errors.warn("G7: failed to analyze " + dot + ": " + e.getMessage());
                }
            }
        }
        return g5Interfaces;
    }

    private void runG6IfNeeded(
            GraphFileMapper mapper,
            Path algorithmsDir,
            AlgorithmsJsonWriter json,
            GraphAlgorithmsErrorHandler errors,
            List<String> g5Interfaces)
            throws IOException {
        Path g6 = mapper.g6();
        if (!Files.isRegularFile(g6)) {
            errors.warn("G6: missing file " + g6.toAbsolutePath());
            return;
        }
        if (g5Interfaces.isEmpty()) {
            errors.warn(
                    "G6: no interfaces from G5; writing empty g6_algorithms.json (file present: "
                            + g6.toAbsolutePath()
                            + ")");
        }
        try {
            DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(g6);
            G6AlgorithmsDocument doc = buildG6(g, g5Interfaces);
            json.write(algorithmsDir.resolve("g6_algorithms.json"), doc);
        } catch (Exception e) {
            errors.warn("G6: failed to analyze " + g6 + ": " + e.getMessage());
        }
    }

    private void runG3(
            GraphFileMapper mapper,
            Path algorithmsDir,
            AlgorithmsJsonWriter json,
            GraphAlgorithmsErrorHandler errors,
            boolean runClustering)
            throws IOException {
        List<GraphFileMapper.MirroredDot> g3dots = mapper.listMirroredLayerDots("g3_method_calls");
        if (g3dots.isEmpty()) {
            errors.warn("G3: no .dot files under any .../g3_method_calls/ directory");
            return;
        }
        for (GraphFileMapper.MirroredDot md : g3dots) {
            try {
                DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(md.dotFile());
                G3AlgorithmsDocument doc = buildG3(g, runClustering);
                Path outDir =
                        algorithmsDir
                                .resolve(md.mirrorRelativeToGraphs())
                                .resolve("g3_algorithms");
                Files.createDirectories(outDir);
                json.write(outDir.resolve(md.fileStem() + ".json"), doc);
            } catch (Exception ex) {
                errors.warn("G3: failed " + md.dotFile() + ": " + ex.getMessage());
            }
        }
    }

    private void runG4Field(
            GraphFileMapper mapper,
            Path algorithmsDir,
            AlgorithmsJsonWriter json,
            GraphAlgorithmsErrorHandler errors)
            throws IOException {
        List<GraphFileMapper.MirroredDot> dots = mapper.listMirroredLayerDots("g4_field_usage");
        if (dots.isEmpty()) {
            errors.warn("G4 field: no .dot files under any .../g4_field_usage/ directory");
            return;
        }
        for (GraphFileMapper.MirroredDot md : dots) {
            try {
                DirectedPseudograph<String, DefaultEdge> g = GraphDotLoader.loadDirected(md.dotFile());
                G4FieldAlgorithmsDocument doc = buildG4Field(g);
                Path outDir =
                        algorithmsDir
                                .resolve(md.mirrorRelativeToGraphs())
                                .resolve("g4_field_algorithms");
                Files.createDirectories(outDir);
                json.write(outDir.resolve(md.fileStem() + ".json"), doc);
            } catch (Exception ex) {
                errors.warn("G4 field: failed " + md.dotFile() + ": " + ex.getMessage());
            }
        }
    }

    private void runG4Projection(
            GraphFileMapper mapper,
            Path algorithmsDir,
            AlgorithmsJsonWriter json,
            GraphAlgorithmsErrorHandler errors,
            boolean runClustering)
            throws IOException {
        List<GraphFileMapper.MirroredDot> dots = mapper.listMirroredLayerDots("g4_method_projection");
        if (dots.isEmpty()) {
            errors.warn("G4 projection: no .dot files under any .../g4_method_projection/ directory");
            return;
        }
        for (GraphFileMapper.MirroredDot md : dots) {
            try {
                Multigraph<String, DefaultEdge> g = GraphDotLoader.loadUndirected(md.dotFile());
                G4ProjectionAlgorithmsDocument doc = buildG4Projection(g, runClustering);
                Path outDir =
                        algorithmsDir
                                .resolve(md.mirrorRelativeToGraphs())
                                .resolve("g4_projection_algorithms");
                Files.createDirectories(outDir);
                json.write(outDir.resolve(md.fileStem() + ".json"), doc);
            } catch (Exception ex) {
                errors.warn("G4 projection: failed " + md.dotFile() + ": " + ex.getMessage());
            }
        }
    }

    private static G1AlgorithmsDocument buildG1(
            DirectedPseudograph<String, DefaultEdge> g, boolean runClustering) {
        G1AlgorithmsDocument doc = new G1AlgorithmsDocument();
        doc.stronglyConnectedComponents = SccKosarajuAnalyzer.sortedSccStrings(g);
        doc.inDegree = sortMap(DegreeMetricsAnalyzer.inDegrees(g));
        doc.outDegree = sortMap(DegreeMetricsAnalyzer.outDegrees(g));
        var cent = DegreeCentralityAnalyzer.directed(g);
        doc.degreeCentrality = sortMapDouble(cent.degreeCentrality());
        doc.inCentrality = sortMapDouble(cent.inCentrality());
        doc.outCentrality = sortMapDouble(cent.outCentrality());
        if (runClustering) {
            DirectedPseudograph<String, DefaultEdge> s = simpleDirectedForClustering(g);
            var w =
                    LouvainClusteringAnalyzer.undirectedWeightsFromDirected(
                            s.vertexSet(), s.edgeSet(), s::getEdgeSource, s::getEdgeTarget);
            doc.clusters =
                    LouvainClusteringAnalyzer.cluster(
                            w, LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED);
        } else {
            doc.clusters = List.of();
        }
        return doc;
    }

    private static G2AlgorithmsDocument buildG2(DirectedPseudograph<String, DefaultEdge> g) {
        G2AlgorithmsDocument doc = new G2AlgorithmsDocument();
        doc.inDegree = sortMap(DegreeMetricsAnalyzer.inDegrees(g));
        var lp = LongestPathDagAnalyzer.longestPathsFromRoots(g);
        if (lp.dagOk) {
            doc.roots = new ArrayList<>();
            for (var r : lp.roots) {
                doc.roots.add(r);
            }
            doc.maxDepthFromRootEdges = new LinkedHashMap<>();
            for (var r : lp.roots) {
                doc.maxDepthFromRootEdges.put(r.toString(), lp.maxDepthEdgesByRoot.get(r));
            }
            doc.maxDepthFromRootEdges = sortMap(doc.maxDepthFromRootEdges);
            doc.globalLongestPathEdges = lp.globalLongestPathEdges;
            doc.longestPathNote = lp.note;
        } else {
            doc.roots = List.of();
            doc.maxDepthFromRootEdges = Map.of();
            doc.globalLongestPathEdges = 0;
            doc.longestPathNote = lp.note;
        }
        return doc;
    }

    private static G3AlgorithmsDocument buildG3(
            DirectedPseudograph<String, DefaultEdge> g, boolean runClustering) {
        G3AlgorithmsDocument doc = new G3AlgorithmsDocument();
        doc.stronglyConnectedComponents = SccKosarajuAnalyzer.sortedSccStrings(g);
        doc.inDegree = sortMap(DegreeMetricsAnalyzer.inDegrees(g));
        doc.outDegree = sortMap(DegreeMetricsAnalyzer.outDegrees(g));
        doc.isolatedNodes = IsolatedNodesAnalyzer.isolatedDirected(g);
        if (runClustering) {
            DirectedPseudograph<String, DefaultEdge> s = simpleDirectedForClustering(g);
            var w =
                    LouvainClusteringAnalyzer.undirectedWeightsFromDirected(
                            s.vertexSet(), s.edgeSet(), s::getEdgeSource, s::getEdgeTarget);
            doc.clusters =
                    LouvainClusteringAnalyzer.cluster(
                            w, LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED);
        } else {
            doc.clusters = List.of();
        }
        return doc;
    }

    private static G4FieldAlgorithmsDocument buildG4Field(
            DirectedPseudograph<String, DefaultEdge> g) {
        G4FieldAlgorithmsDocument doc = new G4FieldAlgorithmsDocument();
        Map<String, Integer> mOut = new TreeMap<>();
        Map<String, Integer> fIn = new TreeMap<>();
        for (String v : g.vertexSet()) {
            if (v.startsWith("m_")) {
                int c = 0;
                for (DefaultEdge e : g.outgoingEdgesOf(v)) {
                    String t = org.jgrapht.Graphs.getOppositeVertex(g, e, v);
                    if (t.startsWith("f_")) {
                        c++;
                    }
                }
                mOut.put(v, c);
            } else if (v.startsWith("f_")) {
                fIn.put(v, g.inDegreeOf(v));
            }
        }
        doc.methodOutDegree = mOut;
        doc.fieldInDegree = fIn;
        doc.lcom = LcomAnalyzer.lcomForFieldUsageGraph(g);
        return doc;
    }

    private static G4ProjectionAlgorithmsDocument buildG4Projection(
            Multigraph<String, DefaultEdge> g, boolean runClustering) {
        G4ProjectionAlgorithmsDocument doc = new G4ProjectionAlgorithmsDocument();
        doc.connectedComponents = ConnectedComponentsAnalyzer.sortedComponentLists(g);
        doc.isolatedNodes = IsolatedNodesAnalyzer.isolatedUndirected(g);
        if (runClustering) {
            SimpleGraph<String, DefaultEdge> s = simpleUndirected(g);
            var w =
                    LouvainClusteringAnalyzer.undirectedWeightsFromUndirected(
                            s.vertexSet(), s.edgeSet(), s::getEdgeSource, s::getEdgeTarget);
            doc.clusters =
                    LouvainClusteringAnalyzer.cluster(
                            w, LouvainClusteringAnalyzer.DEFAULT_CLUSTERING_SEED);
        } else {
            doc.clusters = List.of();
        }
        return doc;
    }

    private static G5AlgorithmsDocument buildG5(
            DirectedPseudograph<String, DefaultEdge> g, Set<String> interfaceIds) {
        G5AlgorithmsDocument doc = new G5AlgorithmsDocument();
        doc.interfaceInDegree = new TreeMap<>();
        doc.classOutDegree = new TreeMap<>();
        for (String v : g.vertexSet()) {
            if (interfaceIds.contains(v)) {
                doc.interfaceInDegree.put(v, g.inDegreeOf(v));
            } else {
                doc.classOutDegree.put(v, g.outDegreeOf(v));
            }
        }
        List<String> zeroIn = new ArrayList<>();
        for (String iface : doc.interfaceInDegree.keySet()) {
            if (doc.interfaceInDegree.get(iface) == 0) {
                zeroIn.add(iface);
            }
        }
        doc.interfacesWithZeroInDegree = zeroIn;
        return doc;
    }

    private static G6AlgorithmsDocument buildG6(
            DirectedPseudograph<String, DefaultEdge> g, List<String> knownInterfaces) {
        G6AlgorithmsDocument doc = new G6AlgorithmsDocument();
        doc.knownInterfacesFromG5 = new ArrayList<>(knownInterfaces);
        doc.interfaceInDegree = new TreeMap<>();
        for (String iface : knownInterfaces) {
            int d = g.containsVertex(iface) ? g.inDegreeOf(iface) : 0;
            doc.interfaceInDegree.put(iface, d);
        }
        List<String> zero = new ArrayList<>();
        for (Map.Entry<String, Integer> e : doc.interfaceInDegree.entrySet()) {
            if (e.getValue() == 0) {
                zero.add(e.getKey());
            }
        }
        doc.interfacesWithZeroInDegree = zero;
        return doc;
    }

    private static <V, E> Map<String, Integer> sortMap(Map<String, Integer> m) {
        return new TreeMap<>(m);
    }

    private static Map<String, Double> sortMapDouble(Map<String, Double> m) {
        return new TreeMap<>(m);
    }

    /** One directed arc per ordered pair — deduplicates parallel arcs before Louvain. */
    private static DirectedPseudograph<String, DefaultEdge> simpleDirected(
            DirectedPseudograph<String, DefaultEdge> g) {
        DirectedPseudograph<String, DefaultEdge> s = new DirectedPseudograph<>(DefaultEdge.class);
        for (String v : g.vertexSet()) {
            s.addVertex(v);
        }
        for (DefaultEdge e : g.edgeSet()) {
            String a = g.getEdgeSource(e);
            String b = g.getEdgeTarget(e);
            if (!s.containsEdge(a, b)) {
                s.addEdge(a, b);
            }
        }
        return s;
    }

    /**
     * Collapses parallel arcs then drops self-loops before Louvain (G1 may contain self-arcs).
     */
    private static DirectedPseudograph<String, DefaultEdge> simpleDirectedForClustering(
            DirectedPseudograph<String, DefaultEdge> g) {
        DirectedPseudograph<String, DefaultEdge> s = simpleDirected(g);
        for (String v : new ArrayList<>(s.vertexSet())) {
            s.removeAllEdges(v, v);
        }
        return s;
    }

    /** One undirected edge per vertex pair for clustering. */
    private static SimpleGraph<String, DefaultEdge> simpleUndirected(Multigraph<String, DefaultEdge> g) {
        SimpleGraph<String, DefaultEdge> s = new SimpleGraph<>(DefaultEdge.class);
        for (String v : g.vertexSet()) {
            s.addVertex(v);
        }
        for (DefaultEdge e : g.edgeSet()) {
            String a = g.getEdgeSource(e);
            String b = g.getEdgeTarget(e);
            if (!s.containsEdge(a, b)) {
                s.addEdge(a, b);
            }
        }
        return s;
    }
}

package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.TopologicalOrderIterator;

/**
 * Longest path (edge count) in a DAG using JGraphT {@link TopologicalOrderIterator} and {@link
 * CycleDetector} for validation.
 */
public final class LongestPathDagAnalyzer {

    private LongestPathDagAnalyzer() {}

    public static <V, E> DagLongestPathResult<V> longestPathFromSource(
            Graph<V, E> graph, V source) {
        CycleDetector<V, E> cycles = new CycleDetector<>(graph);
        if (cycles.detectCycles()) {
            return DagLongestPathResult.error("Graph contains a cycle; longest path in DAG undefined.");
        }
        Map<V, Integer> best = new HashMap<>();
        best.put(source, 0);
        TopologicalOrderIterator<V, E> topo = new TopologicalOrderIterator<>(graph);
        while (topo.hasNext()) {
            V u = topo.next();
            Integer du = best.get(u);
            if (du == null) {
                continue;
            }
            for (E e : graph.outgoingEdgesOf(u)) {
                V v = Graphs.getOppositeVertex(graph, e, u);
                best.merge(v, du + 1, Integer::max);
            }
        }
        int global = best.values().stream().max(Integer::compareTo).orElse(0);
        return DagLongestPathResult.ok(global, null);
    }

    /**
     * For each root (typically in-degree zero), longest path edge count from that root over nodes
     * reachable from it.
     */
    public static <V, E> MultiRootLongestPathResult<V> longestPathsFromRoots(Graph<V, E> graph) {
        CycleDetector<V, E> cycles = new CycleDetector<>(graph);
        if (cycles.detectCycles()) {
            return MultiRootLongestPathResult.error(
                    "Graph contains a cycle; longest path from roots undefined.");
        }
        List<V> roots = new ArrayList<>();
        for (V v : graph.vertexSet()) {
            if (graph.inDegreeOf(v) == 0) {
                roots.add(v);
            }
        }
        roots.sort(Comparator.comparing(Object::toString));
        Map<V, Integer> maxFromRoot = new LinkedHashMap<>();
        int globalMax = 0;
        List<V> topoList = new ArrayList<>();
        new TopologicalOrderIterator<>(graph).forEachRemaining(topoList::add);
        for (V root : roots) {
            Map<V, Integer> best = new HashMap<>();
            best.put(root, 0);
            for (V u : topoList) {
                Integer du = best.get(u);
                if (du == null) {
                    continue;
                }
                for (E e : graph.outgoingEdgesOf(u)) {
                    V v = Graphs.getOppositeVertex(graph, e, u);
                    best.merge(v, du + 1, Integer::max);
                }
            }
            int localMax = best.values().stream().max(Integer::compareTo).orElse(0);
            maxFromRoot.put(root, localMax);
            globalMax = Math.max(globalMax, localMax);
        }
        return MultiRootLongestPathResult.ok(roots, maxFromRoot, globalMax, null);
    }

    public static final class DagLongestPathResult<V> {
        public final boolean dagOk;
        public final int longestPathEdges;
        public final String note;

        private DagLongestPathResult(boolean dagOk, int longestPathEdges, String note) {
            this.dagOk = dagOk;
            this.longestPathEdges = longestPathEdges;
            this.note = note;
        }

        static <V> DagLongestPathResult<V> ok(int edges, String note) {
            return new DagLongestPathResult<>(true, edges, note);
        }

        static <V> DagLongestPathResult<V> error(String note) {
            return new DagLongestPathResult<>(false, 0, note);
        }
    }

    public static final class MultiRootLongestPathResult<V> {
        public final boolean dagOk;
        public final List<V> roots;
        public final Map<V, Integer> maxDepthEdgesByRoot;
        public final int globalLongestPathEdges;
        public final String note;

        private MultiRootLongestPathResult(
                boolean dagOk,
                List<V> roots,
                Map<V, Integer> maxDepthEdgesByRoot,
                int globalLongestPathEdges,
                String note) {
            this.dagOk = dagOk;
            this.roots = roots;
            this.maxDepthEdgesByRoot = maxDepthEdgesByRoot;
            this.globalLongestPathEdges = globalLongestPathEdges;
            this.note = note;
        }

        static <V> MultiRootLongestPathResult<V> ok(
                List<V> roots, Map<V, Integer> byRoot, int global, String note) {
            return new MultiRootLongestPathResult<>(
                    true, Collections.unmodifiableList(new ArrayList<>(roots)), byRoot, global, note);
        }

        static <V> MultiRootLongestPathResult<V> error(String note) {
            return new MultiRootLongestPathResult<>(
                    false, List.of(), Map.of(), 0, note);
        }
    }
}

package com.solidanalysis.algorithms.algorithms;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jgrapht.Graph;

/** In/out/total degree using JGraphT {@link Graph} degree APIs. */
public final class DegreeMetricsAnalyzer {

    private DegreeMetricsAnalyzer() {}

    public static <V, E> Map<String, Integer> inDegrees(Graph<V, E> graph) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (V v : graph.vertexSet()) {
            m.put(v.toString(), graph.inDegreeOf(v));
        }
        return m;
    }

    public static <V, E> Map<String, Integer> outDegrees(Graph<V, E> graph) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (V v : graph.vertexSet()) {
            m.put(v.toString(), graph.outDegreeOf(v));
        }
        return m;
    }

    public static <V, E> Map<String, Integer> undirectedDegrees(Graph<V, E> graph) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (V v : graph.vertexSet()) {
            m.put(v.toString(), graph.degreeOf(v));
        }
        return m;
    }
}

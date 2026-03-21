package com.solidanalysis.algorithms.algorithms;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jgrapht.Graph;

/**
 * Degree centrality for directed graphs (FR-003): {@code (inDegree(v) + outDegree(v)) / (2 * (n -
 * 1))}, plus normalized in/out for reporting.
 */
public final class DegreeCentralityAnalyzer {

    private DegreeCentralityAnalyzer() {}

    public static <V, E> DirectedDegreeCentrality directed(Graph<V, E> graph) {
        int n = graph.vertexSet().size();
        Map<String, Double> combined = new LinkedHashMap<>();
        Map<String, Double> inC = new LinkedHashMap<>();
        Map<String, Double> outC = new LinkedHashMap<>();
        if (n <= 1) {
            for (V v : graph.vertexSet()) {
                String id = v.toString();
                combined.put(id, 0.0);
                inC.put(id, 0.0);
                outC.put(id, 0.0);
            }
            return new DirectedDegreeCentrality(combined, inC, outC);
        }
        double denom = 2.0 * (n - 1);
        double denomSingle = n - 1.0;
        for (V v : graph.vertexSet()) {
            int in = graph.inDegreeOf(v);
            int out = graph.outDegreeOf(v);
            String id = v.toString();
            combined.put(id, (in + out) / denom);
            inC.put(id, in / denomSingle);
            outC.put(id, out / denomSingle);
        }
        return new DirectedDegreeCentrality(combined, inC, outC);
    }

    public record DirectedDegreeCentrality(
            Map<String, Double> degreeCentrality,
            Map<String, Double> inCentrality,
            Map<String, Double> outCentrality) {}
}

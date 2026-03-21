package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jgrapht.Graph;

/** Isolated vertices using JGraphT degree APIs. */
public final class IsolatedNodesAnalyzer {

    private IsolatedNodesAnalyzer() {}

    public static <V, E> List<String> isolatedDirected(Graph<V, E> graph) {
        List<String> ids = new ArrayList<>();
        for (V v : graph.vertexSet()) {
            if (graph.inDegreeOf(v) == 0 && graph.outDegreeOf(v) == 0) {
                ids.add(v.toString());
            }
        }
        Collections.sort(ids);
        return ids;
    }

    public static <V, E> List<String> isolatedUndirected(Graph<V, E> graph) {
        List<String> ids = new ArrayList<>();
        for (V v : graph.vertexSet()) {
            if (graph.degreeOf(v) == 0) {
                ids.add(v.toString());
            }
        }
        Collections.sort(ids);
        return ids;
    }
}

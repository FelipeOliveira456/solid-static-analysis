package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;

/**
 * Strongly connected components via JGraphT {@link KosarajuStrongConnectivityInspector}.
 */
public final class SccKosarajuAnalyzer {

    private SccKosarajuAnalyzer() {}

    public static <V, E> List<List<V>> sortedScc(Graph<V, E> graph) {
        KosarajuStrongConnectivityInspector<V, E> inspector =
                new KosarajuStrongConnectivityInspector<>(graph);
        List<List<V>> out = new ArrayList<>();
        for (var set : inspector.stronglyConnectedSets()) {
            List<V> sorted = new ArrayList<>(set);
            sorted.sort(Comparator.comparing(Object::toString));
            out.add(sorted);
        }
        out.sort(Comparator.comparing(l -> l.isEmpty() ? "" : l.get(0).toString()));
        return out;
    }

    public static <V, E> List<List<String>> sortedSccStrings(Graph<V, E> graph) {
        List<List<V>> scc = sortedScc(graph);
        List<List<String>> result = new ArrayList<>();
        for (List<V> comp : scc) {
            List<String> row = new ArrayList<>();
            for (V v : comp) {
                row.add(v.toString());
            }
            result.add(Collections.unmodifiableList(row));
        }
        return result;
    }
}

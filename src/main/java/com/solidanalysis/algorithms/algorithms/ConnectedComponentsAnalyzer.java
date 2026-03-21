package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;

/**
 * Undirected connected components via JGraphT {@link ConnectivityInspector}.
 */
public final class ConnectedComponentsAnalyzer {

    private ConnectedComponentsAnalyzer() {}

    public static <V, E> List<List<String>> sortedComponentLists(Graph<V, E> graph) {
        ConnectivityInspector<V, E> inspector = new ConnectivityInspector<>(graph);
        List<List<String>> out = new ArrayList<>();
        for (Set<V> comp : inspector.connectedSets()) {
            List<String> row = new ArrayList<>();
            for (V v : comp) {
                row.add(v.toString());
            }
            Collections.sort(row);
            out.add(row);
        }
        out.sort(Comparator.comparing(l -> l.isEmpty() ? "" : l.get(0)));
        return out;
    }
}

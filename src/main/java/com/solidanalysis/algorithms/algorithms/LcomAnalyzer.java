package com.solidanalysis.algorithms.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;

/**
 * LCOM on G4 field-usage graphs ({@code m_*} → {@code f_*}), using neighborhood inspection on the
 * JGraphT graph.
 */
public final class LcomAnalyzer {

    private LcomAnalyzer() {}

    public static double lcomForFieldUsageGraph(Graph<String, DefaultEdge> graph) {
        List<String> methods = new ArrayList<>();
        for (String v : graph.vertexSet()) {
            if (v.startsWith("m_")) {
                methods.add(v);
            }
        }
        Collections.sort(methods);
        if (methods.size() < 2) {
            return 0.0;
        }
        Map<String, Set<String>> methodFields = new HashMap<>();
        for (String m : methods) {
            Set<String> fields = new HashSet<>();
            for (DefaultEdge e : graph.outgoingEdgesOf(m)) {
                String t = Graphs.getOppositeVertex(graph, e, m);
                if (t.startsWith("f_")) {
                    fields.add(t);
                }
            }
            methodFields.put(m, fields);
        }
        int totalPairs = methods.size() * (methods.size() - 1) / 2;
        int disjointPairs = 0;
        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                Set<String> a = methodFields.get(methods.get(i));
                Set<String> b = methodFields.get(methods.get(j));
                if (Collections.disjoint(a, b)) {
                    disjointPairs++;
                }
            }
        }
        return disjointPairs / (double) totalPairs;
    }
}

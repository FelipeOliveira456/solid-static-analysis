package com.solidanalysis.algorithms.algorithms;

import com.solidanalysis.algorithms.model.G7AlgorithmsDocument;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/** CFG metrics for G7 DOT graphs (decision {@code s_*} nodes, longest path from {@code entry}). */
public final class G7CfgAnalyzer {

    private static final String ENTRY = "entry";

    private G7CfgAnalyzer() {}

    public static G7AlgorithmsDocument analyze(Graph<String, DefaultEdge> graph) {
        G7AlgorithmsDocument doc = new G7AlgorithmsDocument();
        int decisionCount = 0;
        int maxDecisionOut = 0;
        for (String v : graph.vertexSet()) {
            if (v.startsWith("s_")) {
                decisionCount++;
                maxDecisionOut = Math.max(maxDecisionOut, graph.outDegreeOf(v));
            }
        }
        doc.decisionNodeCount = decisionCount;
        doc.maxDecisionOutDegree = maxDecisionOut;
        if (!graph.containsVertex(ENTRY)) {
            doc.longestPathEdgesFromEntry = 0;
            doc.longestPathNote = "Missing vertex '" + ENTRY + "'.";
            return doc;
        }
        var path = LongestPathDagAnalyzer.longestPathFromSource(graph, ENTRY);
        if (path.dagOk) {
            doc.longestPathEdgesFromEntry = path.longestPathEdges;
            doc.longestPathNote = path.note;
        } else {
            doc.longestPathEdgesFromEntry = 0;
            doc.longestPathNote = path.note;
        }
        return doc;
    }
}

package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solidanalysis.algorithms.algorithms.DegreeMetricsAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class DegreeMetricsAnalyzerTest {

    @Test
    void inOutDegreesWithParallelEdges() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("X");
        g.addVertex("Y");
        g.addEdge("X", "Y");
        g.addEdge("X", "Y");
        g.addEdge("Y", "X");
        assertEquals(1, DegreeMetricsAnalyzer.inDegrees(g).get("X"));
        assertEquals(2, DegreeMetricsAnalyzer.outDegrees(g).get("X"));
        assertEquals(2, DegreeMetricsAnalyzer.inDegrees(g).get("Y"));
        assertEquals(1, DegreeMetricsAnalyzer.outDegrees(g).get("Y"));
    }
}

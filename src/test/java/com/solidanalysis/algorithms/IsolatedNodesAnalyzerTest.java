package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solidanalysis.algorithms.algorithms.IsolatedNodesAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class IsolatedNodesAnalyzerTest {

    @Test
    void directedIsolatedDetected() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("I");
        g.addEdge("A", "B");
        assertEquals(java.util.List.of("I"), IsolatedNodesAnalyzer.isolatedDirected(g));
    }
}

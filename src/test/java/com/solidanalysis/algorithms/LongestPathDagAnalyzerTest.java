package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.algorithms.algorithms.LongestPathDagAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class LongestPathDagAnalyzerTest {

    @Test
    void longestChainFourVertices() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addVertex("C");
        g.addVertex("D");
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        g.addEdge("C", "D");
        var r = LongestPathDagAnalyzer.longestPathsFromRoots(g);
        assertTrue(r.dagOk);
        assertEquals(3, r.globalLongestPathEdges);
    }

    @Test
    void singleRootAndChild() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge("A", "B");
        var r = LongestPathDagAnalyzer.longestPathsFromRoots(g);
        assertTrue(r.dagOk);
        assertEquals(1, r.globalLongestPathEdges);
    }
}

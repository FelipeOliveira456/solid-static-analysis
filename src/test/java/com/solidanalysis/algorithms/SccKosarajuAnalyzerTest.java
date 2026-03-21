package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.algorithms.algorithms.SccKosarajuAnalyzer;
import java.util.List;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class SccKosarajuAnalyzerTest {

    @Test
    void detectsTwoNodeCycle() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge("A", "B");
        g.addEdge("B", "A");
        List<List<String>> scc = SccKosarajuAnalyzer.sortedSccStrings(g);
        assertEquals(1, scc.size());
        assertEquals(2, scc.get(0).size());
        assertTrue(scc.get(0).contains("A"));
        assertTrue(scc.get(0).contains("B"));
    }

    @Test
    void acyclicGraphYieldsSingletonComponents() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("A");
        g.addVertex("B");
        g.addEdge("A", "B");
        List<List<String>> scc = SccKosarajuAnalyzer.sortedSccStrings(g);
        assertEquals(2, scc.size());
        assertEquals(List.of("A"), scc.get(0));
        assertEquals(List.of("B"), scc.get(1));
    }
}

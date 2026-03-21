package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.algorithms.algorithms.ConnectedComponentsAnalyzer;
import com.solidanalysis.algorithms.algorithms.IsolatedNodesAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.Multigraph;
import org.junit.jupiter.api.Test;

class ConnectedComponentsAnalyzerTest {

    @Test
    void twoComponentsAndIsolated() {
        Multigraph<String, DefaultEdge> g = new Multigraph<>(DefaultEdge.class);
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addVertex("d");
        g.addVertex("iso");
        g.addEdge("a", "b");
        g.addEdge("c", "d");
        var comps = ConnectedComponentsAnalyzer.sortedComponentLists(g);
        assertEquals(3, comps.size());
        assertTrue(comps.contains(java.util.List.of("a", "b")));
        assertTrue(comps.contains(java.util.List.of("c", "d")));
        assertTrue(comps.contains(java.util.List.of("iso")));
        assertEquals(java.util.List.of("iso"), IsolatedNodesAnalyzer.isolatedUndirected(g));
    }
}

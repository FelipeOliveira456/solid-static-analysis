package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solidanalysis.algorithms.algorithms.DegreeCentralityAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class DegreeCentralityAnalyzerTest {

    /**
     * Hub with mutual edges to every leaf: in+out = 2(n-1) so FR-003 formula yields 1.0 at center.
     */
    @Test
    void degreeCentralityOneAtCenterOfMutualStar() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        String c = "C";
        g.addVertex(c);
        for (int i = 0; i < 3; i++) {
            String leaf = "L" + i;
            g.addVertex(leaf);
            g.addEdge(c, leaf);
            g.addEdge(leaf, c);
        }
        var r = DegreeCentralityAnalyzer.directed(g);
        assertEquals(1.0, r.degreeCentrality().get(c), 1e-9);
    }
}

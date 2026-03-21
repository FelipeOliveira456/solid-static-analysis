package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solidanalysis.algorithms.algorithms.DegreeMetricsAnalyzer;
import com.solidanalysis.algorithms.algorithms.LcomAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class LcomAnalyzerTest {

    @Test
    void disjointFieldSetsYieldLcomOne() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("m_1");
        g.addVertex("m_2");
        g.addVertex("f_a");
        g.addVertex("f_b");
        g.addEdge("m_1", "f_a");
        g.addEdge("m_2", "f_b");
        assertEquals(1.0, LcomAnalyzer.lcomForFieldUsageGraph(g), 1e-9);
    }

    @Test
    void identicalFieldSetsYieldLcomZero() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("m_1");
        g.addVertex("m_2");
        g.addVertex("f_x");
        g.addEdge("m_1", "f_x");
        g.addEdge("m_2", "f_x");
        assertEquals(0.0, LcomAnalyzer.lcomForFieldUsageGraph(g), 1e-9);
    }

    @Test
    void lcomHalfWhenThreeOfSixPairsAreDisjoint() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("m_1");
        g.addVertex("m_2");
        g.addVertex("m_3");
        g.addVertex("m_4");
        g.addVertex("f_x");
        g.addVertex("f_y");
        g.addEdge("m_1", "f_x");
        g.addEdge("m_2", "f_x");
        g.addEdge("m_3", "f_x");
        g.addEdge("m_4", "f_y");
        assertEquals(0.5, LcomAnalyzer.lcomForFieldUsageGraph(g), 1e-9);
        var out = DegreeMetricsAnalyzer.outDegrees(g);
        assertEquals(1, out.get("m_1"));
        assertEquals(1, out.get("m_4"));
        var fin = DegreeMetricsAnalyzer.inDegrees(g);
        assertEquals(3, fin.get("f_x"));
        assertEquals(1, fin.get("f_y"));
    }
}

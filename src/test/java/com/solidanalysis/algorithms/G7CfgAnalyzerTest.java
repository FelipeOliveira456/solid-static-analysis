package com.solidanalysis.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.solidanalysis.algorithms.algorithms.G7CfgAnalyzer;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.junit.jupiter.api.Test;

class G7CfgAnalyzerTest {

    @Test
    void decisionCountIgnoresEntryThenElse() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("entry");
        g.addVertex("s_1");
        g.addVertex("then_x");
        g.addVertex("else_y");
        g.addEdge("entry", "s_1");
        var doc = G7CfgAnalyzer.analyze(g);
        assertEquals(1, doc.decisionNodeCount);
    }

    @Test
    void nestedLongestPathDepthFourEdges() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("entry");
        g.addVertex("s_1");
        g.addVertex("then_1");
        g.addVertex("s_2");
        g.addVertex("then_2");
        g.addEdge("entry", "s_1");
        g.addEdge("s_1", "then_1");
        g.addEdge("then_1", "s_2");
        g.addEdge("s_2", "then_2");
        var doc = G7CfgAnalyzer.analyze(g);
        assertEquals(4, doc.longestPathEdgesFromEntry);
        assertEquals(2, doc.decisionNodeCount);
        assertEquals(1, doc.maxDecisionOutDegree);
    }

    @Test
    void maxDecisionOutDegreeAmongSNodes() {
        DirectedMultigraph<String, DefaultEdge> g = new DirectedMultigraph<>(DefaultEdge.class);
        g.addVertex("entry");
        g.addVertex("s_1");
        g.addVertex("s_2");
        g.addVertex("a");
        g.addVertex("b");
        g.addVertex("c");
        g.addEdge("entry", "s_1");
        g.addEdge("s_1", "a");
        g.addEdge("s_1", "b");
        g.addEdge("s_1", "c");
        g.addEdge("s_2", "a");
        var doc = G7CfgAnalyzer.analyze(g);
        assertEquals(3, doc.maxDecisionOutDegree);
    }
}

package com.solidanalysis.graphs.dot;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DotWriterTest {

    @Test
    void grafoDeterministicoELabelsEscapados() {
        DirectedDotGraph g = new DirectedDotGraph("T");
        g.addNode("n1", "a\"b", Map.of());
        g.addNode("n2", "y", Map.of());
        g.addEdge("n1", "n2", "call");
        String dot = g.toDot();
        assertTrue(dot.contains("digraph T"));
        assertTrue(dot.contains("n1 -> n2"));
        String escaped = DotText.escapeLabel("a\"b");
        assertTrue(dot.contains(escaped));
    }
}

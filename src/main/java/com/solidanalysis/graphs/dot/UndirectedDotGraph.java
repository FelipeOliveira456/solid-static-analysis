package com.solidanalysis.graphs.dot;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Mutable undirected graph serialized as a Graphviz {@code graph} (edges {@code --}). */
public final class UndirectedDotGraph {

    private final String graphName;
    private final TreeMap<String, Map<String, String>> nodes = new TreeMap<>();
    private final TreeSet<UEdge> edges =
            new TreeSet<>(
                    Comparator.comparing(UEdge::a)
                            .thenComparing(UEdge::b)
                            .thenComparing(UEdge::label, Comparator.nullsFirst(String::compareTo)));

    public UndirectedDotGraph(String graphName) {
        this.graphName = Objects.requireNonNull(graphName, "graphName");
    }

    /**
     * @param nodeId stable identifier (already sanitized for DOT)
     * @param label human-readable label (will be escaped)
     * @param attributes optional extra DOT attributes
     */
    public void addNode(String nodeId, String label, Map<String, String> attributes) {
        Objects.requireNonNull(nodeId, "nodeId");
        TreeMap<String, String> attrs = new TreeMap<>();
        attrs.put("label", DotText.escapeLabel(label == null ? nodeId : label));
        if (attributes != null) {
            for (var e : attributes.entrySet()) {
                if (!"label".equals(e.getKey()) && e.getValue() != null) {
                    attrs.put(e.getKey(), e.getValue());
                }
            }
        }
        nodes.put(nodeId, attrs);
    }

    /** Undirected edge between {@code a} and {@code b} (order normalized internally). */
    public void addEdge(String a, String b, String label) {
        Objects.requireNonNull(a, "a");
        Objects.requireNonNull(b, "b");
        if (a.compareTo(b) <= 0) {
            edges.add(new UEdge(a, b, label));
        } else {
            edges.add(new UEdge(b, a, label));
        }
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("graph ").append(DotText.sanitizeId(graphName)).append(" {\n");
        for (var e : nodes.entrySet()) {
            sb.append("  ")
                    .append(e.getKey())
                    .append(" [")
                    .append(
                            e.getValue().entrySet().stream()
                                    .map(en -> en.getKey() + "=" + en.getValue())
                                    .collect(Collectors.joining(", ")))
                    .append("];\n");
        }
        for (UEdge e : edges) {
            sb.append("  ").append(e.a).append(" -- ").append(e.b);
            if (e.label != null && !e.label.isEmpty()) {
                sb.append(" [label=").append(DotText.escapeLabel(e.label)).append("]");
            }
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private record UEdge(String a, String b, String label) {}
}

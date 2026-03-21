package com.solidanalysis.graphs.dot;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/** Mutable directed graph serialized as a Graphviz {@code digraph}. */
public final class DirectedDotGraph {

    private final String graphName;
    private final TreeMap<String, Map<String, String>> nodes = new TreeMap<>();
    private final TreeSet<Edge> edges =
            new TreeSet<>(
                    Comparator.comparing(Edge::from)
                            .thenComparing(Edge::to)
                            .thenComparing(Edge::label, Comparator.nullsFirst(String::compareTo)));

    public DirectedDotGraph(String graphName) {
        this.graphName = Objects.requireNonNull(graphName, "graphName");
    }

    /**
     * @param nodeId stable identifier (already sanitized for DOT)
     * @param label human-readable label (will be escaped)
     * @param attributes optional extra DOT attributes (e.g. {@code shape})
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

    public void addEdge(String fromId, String toId, String label) {
        Objects.requireNonNull(fromId, "fromId");
        Objects.requireNonNull(toId, "toId");
        edges.add(new Edge(fromId, toId, label));
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(DotText.sanitizeId(graphName)).append(" {\n");
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
        for (Edge e : edges) {
            sb.append("  ")
                    .append(e.from)
                    .append(" -> ")
                    .append(e.to);
            if (e.label != null && !e.label.isEmpty()) {
                sb.append(" [label=").append(DotText.escapeLabel(e.label)).append("]");
            }
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private record Edge(String from, String to, String label) {}
}

package com.solidanalysis.graphs;

import com.solidanalysis.graphs.dot.DirectedDotGraph;
import com.solidanalysis.graphs.dot.DotText;
import com.solidanalysis.graphs.filter.TypeRelevanceFilter;
import com.solidanalysis.graphs.internal.TypeNames;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * G6 — directed graph from concrete classes to interfaces used as field types, parameters, or call
 * receivers.
 */
public final class G6InterfaceUsageGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void write(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        DirectedDotGraph g = new DirectedDotGraph("G6");
        for (String t : new TreeSet<>(types)) {
            g.addNode(DotText.sanitizeId(t), t, Map.of());
        }
        TreeSet<Edge> edges = new TreeSet<>();
        for (TypeSummary type : project.typesInStableOrder()) {
            String clazz = type.name();
            if (!types.contains(clazz) || !"class".equals(type.kind())) {
                continue;
            }
            for (var field : type.fields()) {
                maybeAdd(edges, types, project, clazz, field.type());
            }
            for (MethodSummary m : type.methods()) {
                for (var p : m.parameters()) {
                    maybeAdd(edges, types, project, clazz, p.type());
                }
                for (var call : m.methodCalls()) {
                    if (call.declaringType() != null) {
                        maybeAdd(edges, types, project, clazz, call.declaringType());
                    }
                }
            }
        }
        for (Edge e : edges) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    private void maybeAdd(
            Set<Edge> edges, Set<String> types, ParsedProject project, String clazz, String rawType) {
        String s = TypeNames.simpleName(rawType);
        if (!filter.isRelevantProjectType(s, types)) {
            return;
        }
        if (project.isInterface(s)) {
            edges.add(new Edge(clazz, s));
        }
    }

    private record Edge(String from, String to) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}

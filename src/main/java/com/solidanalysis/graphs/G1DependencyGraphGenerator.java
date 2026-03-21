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

/** G1 — class dependency graph with labeled edges (field, param, return, call, instantiation). */
public final class G1DependencyGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void write(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        DirectedDotGraph g = new DirectedDotGraph("G1");
        for (String t : new TreeSet<>(types)) {
            g.addNode(DotText.sanitizeId(t), t, Map.of());
        }
        TreeSet<Edge> edges = new TreeSet<>();
        for (TypeSummary type : project.typesInStableOrder()) {
            String from = type.name();
            if (!types.contains(from)) {
                continue;
            }
            for (var field : type.fields()) {
                String to = targetSimple(field.type(), types);
                if (to != null) {
                    edges.add(new Edge(from, to, "field"));
                }
            }
            for (MethodSummary m : type.methods()) {
                String rt = m.returnType();
                if (rt != null) {
                    String to = targetSimple(rt, types);
                    if (to != null) {
                        edges.add(new Edge(from, to, "return"));
                    }
                }
                for (var p : m.parameters()) {
                    String to = targetSimple(p.type(), types);
                    if (to != null) {
                        edges.add(new Edge(from, to, "param"));
                    }
                }
                for (var call : m.methodCalls()) {
                    if (call.declaringType() == null) {
                        continue;
                    }
                    String to = targetSimple(call.declaringType(), types);
                    if (to != null) {
                        edges.add(new Edge(from, to, "call"));
                    }
                }
                for (var ins : m.instantiations()) {
                    String to = targetSimple(ins.type(), types);
                    if (to != null) {
                        edges.add(new Edge(from, to, "instantiation"));
                    }
                }
            }
        }
        for (Edge e : edges) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), e.label);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    private String targetSimple(String raw, Set<String> projectTypes) {
        String simple = TypeNames.simpleName(raw);
        if (!filter.isRelevantProjectType(simple, projectTypes)) {
            return null;
        }
        return simple;
    }

    private record Edge(String from, String to, String label) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            if (c != 0) {
                return c;
            }
            c = to.compareTo(o.to);
            if (c != 0) {
                return c;
            }
            return label.compareTo(o.label);
        }
    }
}

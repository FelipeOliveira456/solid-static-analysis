package com.solidanalysis.graphs;

import com.solidanalysis.graphs.dot.DirectedDotGraph;
import com.solidanalysis.graphs.dot.DotText;
import com.solidanalysis.graphs.filter.TypeRelevanceFilter;
import com.solidanalysis.graphs.internal.TypeNames;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** G2 — inheritance / implementation graph ({@code extends} / {@code implements}). */
public final class G2InheritanceGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void write(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        DirectedDotGraph g = new DirectedDotGraph("G2");
        for (String t : new TreeSet<>(types)) {
            g.addNode(DotText.sanitizeId(t), t, Map.of());
        }
        TreeSet<Edge> edges = new TreeSet<>();
        for (TypeSummary type : project.typesInStableOrder()) {
            String child = type.name();
            if (!types.contains(child)) {
                continue;
            }
            String sup = type.superclass();
            if (sup != null) {
                String parent = TypeNames.simpleName(sup);
                if (filter.isRelevantProjectType(parent, types)) {
                    edges.add(new Edge(child, parent, "extends"));
                }
            }
            for (String iface : type.implementedInterfaces()) {
                String iname = TypeNames.simpleName(iface);
                if (filter.isRelevantProjectType(iname, types)) {
                    edges.add(new Edge(child, iname, "implements"));
                }
            }
        }
        for (Edge e : edges) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), e.label);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
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

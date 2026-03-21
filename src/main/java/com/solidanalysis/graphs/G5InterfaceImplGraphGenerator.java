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

/** G5 — bipartite class/interface implementation graph. */
public final class G5InterfaceImplGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void write(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        DirectedDotGraph g = new DirectedDotGraph("G5");
        for (String t : new TreeSet<>(types)) {
            boolean iface = project.isInterface(t);
            g.addNode(
                    DotText.sanitizeId(t),
                    t,
                    Map.of("shape", iface ? "ellipse" : "box"));
        }
        TreeSet<Edge> edges = new TreeSet<>();
        for (TypeSummary type : project.typesInStableOrder()) {
            if (!"class".equals(type.kind())) {
                continue;
            }
            String clazz = type.name();
            if (!types.contains(clazz)) {
                continue;
            }
            for (String ifaceRaw : type.implementedInterfaces()) {
                String iface = TypeNames.simpleName(ifaceRaw);
                if (filter.isRelevantProjectType(iface, types) && project.isInterface(iface)) {
                    edges.add(new Edge(clazz, iface));
                }
            }
        }
        for (Edge e : edges) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    private record Edge(String from, String to) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}

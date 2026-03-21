package com.solidanalysis.graphs;

import com.solidanalysis.graphs.dot.DirectedDotGraph;
import com.solidanalysis.graphs.dot.UndirectedDotGraph;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * G4 — bipartite method/field graph plus method–method projection sharing the same field access.
 */
public final class G4FieldUsageGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void writeFieldUsage(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        DirectedDotGraph g = new DirectedDotGraph("G4_field");
        TreeSet<String> methodNodes = new TreeSet<>();
        TreeSet<String> fieldNodes = new TreeSet<>();
        TreeSet<Edge3> edges = new TreeSet<>();
        collect(types, project, methodNodes, fieldNodes, edges);
        for (String m : methodNodes) {
            g.addNode(DotText.sanitizeId("m_" + m), m, Map.of("shape", "box"));
        }
        for (String f : fieldNodes) {
            g.addNode(DotText.sanitizeId("f_" + f), f, Map.of("shape", "ellipse"));
        }
        for (Edge3 e : edges) {
            g.addEdge(
                    DotText.sanitizeId("m_" + e.method),
                    DotText.sanitizeId("f_" + e.field),
                    e.access);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    public void writeMethodProjection(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        TreeSet<String> methodNodes = new TreeSet<>();
        TreeSet<String> fieldNodes = new TreeSet<>();
        TreeSet<Edge3> edges = new TreeSet<>();
        collect(types, project, methodNodes, fieldNodes, edges);
        Map<String, List<String>> fieldToMethods = new HashMap<>();
        for (Edge3 e : edges) {
            fieldToMethods.computeIfAbsent(e.field, k -> new ArrayList<>()).add(e.method);
        }
        UndirectedDotGraph g = new UndirectedDotGraph("G4_proj");
        for (String m : methodNodes) {
            g.addNode(DotText.sanitizeId(m), m, Map.of());
        }
        TreeSet<Edge2> proj = new TreeSet<>();
        for (List<String> ms : fieldToMethods.values()) {
            if (ms.size() < 2) {
                continue;
            }
            List<String> sorted = ms.stream().sorted().distinct().toList();
            for (int i = 0; i < sorted.size(); i++) {
                for (int j = i + 1; j < sorted.size(); j++) {
                    String a = sorted.get(i);
                    String b = sorted.get(j);
                    proj.add(new Edge2(a, b));
                }
            }
        }
        for (Edge2 e : proj) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    /** Writes one field-usage graph per class into {@code outputDir}. */
    public void writeFieldUsagePerClass(Path outputDir, ParsedProject project) throws IOException {
        Files.createDirectories(outputDir);
        Set<String> types = project.projectTypeNames();
        for (TypeSummary type : project.typesInStableOrder()) {
            String owner = type.name();
            if (!types.contains(owner)) {
                continue;
            }
            DirectedDotGraph g = new DirectedDotGraph("G4_field_" + owner);
            TreeSet<String> methodNodes = new TreeSet<>();
            TreeSet<String> fieldNodes = new TreeSet<>();
            TreeSet<Edge3> edges = collectPerOwner(types, owner, type.methods());

            for (Edge3 e : edges) {
                methodNodes.add(e.method);
                fieldNodes.add(e.field);
            }
            for (String m : methodNodes) {
                g.addNode(DotText.sanitizeId("m_" + m), m, Map.of("shape", "box"));
            }
            for (String f : fieldNodes) {
                g.addNode(DotText.sanitizeId("f_" + f), f, Map.of("shape", "ellipse"));
            }
            for (Edge3 e : edges) {
                g.addEdge(
                        DotText.sanitizeId("m_" + e.method),
                        DotText.sanitizeId("f_" + e.field),
                        e.access);
            }
            Files.writeString(
                    outputDir.resolve(safeFile(owner) + ".dot"), g.toDot(), StandardCharsets.UTF_8);
        }
    }

    /** Writes one method-projection graph per class into {@code outputDir}. */
    public void writeMethodProjectionPerClass(Path outputDir, ParsedProject project) throws IOException {
        Files.createDirectories(outputDir);
        Set<String> types = project.projectTypeNames();
        for (TypeSummary type : project.typesInStableOrder()) {
            String owner = type.name();
            if (!types.contains(owner)) {
                continue;
            }
            TreeSet<Edge3> edges = collectPerOwner(types, owner, type.methods());
            Map<String, List<String>> fieldToMethods = new HashMap<>();
            for (Edge3 e : edges) {
                fieldToMethods.computeIfAbsent(e.field, k -> new ArrayList<>()).add(e.method);
            }
            UndirectedDotGraph g = new UndirectedDotGraph("G4_proj_" + owner);
            TreeSet<String> methodNodes = new TreeSet<>();
            for (Edge3 e : edges) {
                methodNodes.add(e.method);
            }
            for (String m : methodNodes) {
                g.addNode(DotText.sanitizeId(m), m, Map.of());
            }
            TreeSet<Edge2> proj = new TreeSet<>();
            for (List<String> ms : fieldToMethods.values()) {
                List<String> sorted = ms.stream().sorted().distinct().toList();
                for (int i = 0; i < sorted.size(); i++) {
                    for (int j = i + 1; j < sorted.size(); j++) {
                        // Per-class projection only: all methods are from this owner.
                        proj.add(new Edge2(sorted.get(i), sorted.get(j)));
                    }
                }
            }
            for (Edge2 e : proj) {
                g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
            }
            Files.writeString(
                    outputDir.resolve(safeFile(owner) + ".dot"), g.toDot(), StandardCharsets.UTF_8);
        }
    }

    private static String safeFile(String token) {
        return token.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private TreeSet<Edge3> collectPerOwner(
            Set<String> types, String owner, List<MethodSummary> methods) {
        TreeSet<Edge3> edges = new TreeSet<>();
        for (MethodSummary m : methods) {
            String mk = ParsedProject.methodKey(owner, m);
            for (var fa : m.fieldAccesses()) {
                if (filter.isSystemOutAccess(fa)) {
                    continue;
                }
                String fOwner = TypeNames.simpleName(fa.ownerClass());
                // Include only project fields, even inherited (ownerClass can differ from current class).
                if (!types.contains(fOwner)) {
                    continue;
                }
                String fieldKey = fOwner + "." + fa.fieldName();
                edges.add(new Edge3(mk, fieldKey, fa.accessType()));
            }
        }
        return edges;
    }

    private void collect(
            Set<String> types,
            ParsedProject project,
            Set<String> methodNodes,
            Set<String> fieldNodes,
            Set<Edge3> edges) {
        for (TypeSummary type : project.typesInStableOrder()) {
            String owner = type.name();
            if (!types.contains(owner)) {
                continue;
            }
            TreeSet<Edge3> local = collectPerOwner(types, owner, type.methods());
            for (Edge3 e : local) {
                methodNodes.add(e.method);
                fieldNodes.add(e.field);
                edges.add(e);
            }
        }
    }

    private record Edge3(String method, String field, String access) implements Comparable<Edge3> {
        @Override
        public int compareTo(Edge3 o) {
            int c = method.compareTo(o.method);
            if (c != 0) {
                return c;
            }
            c = field.compareTo(o.field);
            if (c != 0) {
                return c;
            }
            return access.compareTo(o.access);
        }
    }

    private record Edge2(String from, String to) implements Comparable<Edge2> {
        @Override
        public int compareTo(Edge2 o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}

package com.solidanalysis.graphs;

import com.solidanalysis.graphs.dot.DirectedDotGraph;
import com.solidanalysis.graphs.dot.DotText;
import com.solidanalysis.graphs.filter.TypeRelevanceFilter;
import com.solidanalysis.graphs.internal.CallSignatureParser;
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

/** G3 — method call graph ({@code Class.method} nodes). */
public final class G3MethodCallsGraphGenerator {

    private final TypeRelevanceFilter filter = new TypeRelevanceFilter();

    public void write(Path outputFile, ParsedProject project) throws IOException {
        Set<String> types = project.projectTypeNames();
        Set<String> methodKeys = project.projectMethodKeys();
        DirectedDotGraph g = new DirectedDotGraph("G3");
        for (String mk : new TreeSet<>(methodKeys)) {
            g.addNode(DotText.sanitizeId(mk), mk, Map.of());
        }
        TreeSet<Edge> edges = new TreeSet<>();
        for (TypeSummary type : project.typesInStableOrder()) {
            String owner = type.name();
            if (!types.contains(owner)) {
                continue;
            }
            for (MethodSummary m : type.methods()) {
                String caller = ParsedProject.methodKey(owner, m);
                for (var call : m.methodCalls()) {
                    if (call.declaringType() == null) {
                        continue;
                    }
                    String declSimple = TypeNames.simpleName(call.declaringType());
                    if (!filter.isRelevantProjectType(declSimple, types)) {
                        continue;
                    }
                    String calleeMethod =
                            CallSignatureParser.calleeMethodKey(call.declaringType(), call.signature());
                    if (calleeMethod == null) {
                        continue;
                    }
                    String callee = declSimple + "." + calleeMethod;
                    if (methodKeys.contains(callee)) {
                        edges.add(new Edge(caller, callee));
                    }
                }
            }
        }
        for (Edge e : edges) {
            g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
        }
        Files.writeString(outputFile, g.toDot(), StandardCharsets.UTF_8);
    }

    /**
     * Writes one call graph per class under {@code outputDir}. Each file contains only methods from
     * that class as call origins, but may include external target nodes/edges.
     */
    public void writePerClass(Path outputDir, ParsedProject project) throws IOException {
        Files.createDirectories(outputDir);
        Set<String> types = project.projectTypeNames();
        Set<String> methodKeys = project.projectMethodKeys();

        for (TypeSummary type : project.typesInStableOrder()) {
            String owner = type.name();
            if (!types.contains(owner)) {
                continue;
            }
            DirectedDotGraph g = new DirectedDotGraph("G3_" + owner);
            TreeSet<String> ownMethodKeys = new TreeSet<>();
            for (MethodSummary m : type.methods()) {
                String caller = ParsedProject.methodKey(owner, m);
                ownMethodKeys.add(caller);
                g.addNode(DotText.sanitizeId(caller), caller, Map.of());
            }
            TreeSet<Edge> edges = new TreeSet<>();
            for (MethodSummary m : type.methods()) {
                String caller = ParsedProject.methodKey(owner, m);
                for (var call : m.methodCalls()) {
                    if (call.declaringType() == null) {
                        continue;
                    }
                    String declSimple = TypeNames.simpleName(call.declaringType());
                    if (!filter.isRelevantProjectType(declSimple, types)) {
                        continue;
                    }
                    String calleeMethod =
                            CallSignatureParser.calleeMethodKey(call.declaringType(), call.signature());
                    if (calleeMethod == null) {
                        continue;
                    }
                    String callee = declSimple + "." + calleeMethod;
                    if (!methodKeys.contains(callee)) {
                        continue;
                    }
                    // Only this class methods are valid origins; targets may be external.
                    if (ownMethodKeys.contains(caller)) {
                        g.addNode(DotText.sanitizeId(callee), callee, Map.of());
                        edges.add(new Edge(caller, callee));
                    }
                }
            }
            for (Edge e : edges) {
                g.addEdge(DotText.sanitizeId(e.from), DotText.sanitizeId(e.to), null);
            }
            Path out = outputDir.resolve(safeFile(owner) + ".dot");
            Files.writeString(out, g.toDot(), StandardCharsets.UTF_8);
        }
    }

    private static String safeFile(String token) {
        return token.replaceAll("[^a-zA-Z0-9_-]", "_");
    }

    private record Edge(String from, String to) implements Comparable<Edge> {
        @Override
        public int compareTo(Edge o) {
            int c = from.compareTo(o.from);
            return c != 0 ? c : to.compareTo(o.to);
        }
    }
}

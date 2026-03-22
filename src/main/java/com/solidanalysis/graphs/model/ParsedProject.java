package com.solidanalysis.graphs.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * All {@link AstArtifact} instances from one project output directory plus indexes for graph
 * generation.
 */
public final class ParsedProject {

    private final List<AstArtifact> artifacts;
    private final List<PlacedArtifact> placedArtifacts;
    private final Set<String> projectTypeNames;
    private final Map<String, TypeSummary> typesBySimpleName;

    private ParsedProject(
            List<AstArtifact> artifacts,
            List<PlacedArtifact> placedArtifacts,
            Set<String> projectTypeNames,
            Map<String, TypeSummary> typesBySimpleName) {
        this.artifacts = List.copyOf(artifacts);
        this.placedArtifacts = List.copyOf(placedArtifacts);
        this.projectTypeNames = Set.copyOf(projectTypeNames);
        this.typesBySimpleName = Map.copyOf(typesBySimpleName);
    }

    /** Legacy: every artifact treated as if its JSON lived at the project output root. */
    public static ParsedProject fromArtifacts(List<AstArtifact> artifacts) {
        List<PlacedArtifact> placed = new ArrayList<>();
        for (AstArtifact a : artifacts) {
            placed.add(new PlacedArtifact(Path.of(""), a));
        }
        return fromPlaced(placed);
    }

    /**
     * Builds from scanner outputs with mirrored relative directories (parent of each {@code .json}
     * under the project output).
     */
    public static ParsedProject fromPlaced(List<PlacedArtifact> raw) {
        List<PlacedArtifact> placed = new ArrayList<>();
        for (PlacedArtifact pa : raw) {
            AstArtifact norm = normalizeArtifact(pa.artifact());
            placed.add(new PlacedArtifact(pa.relativeOutputDir(), norm));
        }
        placed.sort(
                Comparator.comparing(PlacedArtifact::relativeOutputDir)
                        .thenComparing(PlacedArtifact::simpleTypeName));
        List<AstArtifact> arts = placed.stream().map(PlacedArtifact::artifact).toList();
        Map<String, TypeSummary> byName = new LinkedHashMap<>();
        for (PlacedArtifact pa : placed) {
            TypeSummary t = pa.artifact().primaryType();
            if (t != null && t.name() != null) {
                byName.put(t.name(), t);
            }
        }
        Set<String> names = byName.keySet().stream().collect(Collectors.toUnmodifiableSet());
        return new ParsedProject(arts, placed, names, byName);
    }

    private static AstArtifact normalizeArtifact(AstArtifact a) {
        TypeSummary t = a.primaryType();
        if (t == null) {
            return a;
        }
        return new AstArtifact(
                a.sourceFile(),
                new TypeSummary(
                        t.kind(),
                        t.name(),
                        t.abstractType(),
                        t.superclass(),
                        nullToEmpty(t.implementedInterfaces()),
                        nullToEmptyFields(t.fields()),
                        nullToEmptyMethods(t.methods())));
    }

    private static List<String> nullToEmpty(List<String> list) {
        return list == null ? List.of() : list;
    }

    private static List<FieldSummary> nullToEmptyFields(List<FieldSummary> list) {
        return list == null ? List.of() : list;
    }

    private static List<MethodSummary> nullToEmptyMethods(List<MethodSummary> list) {
        if (list == null) {
            return List.of();
        }
        List<MethodSummary> out = new ArrayList<>(list.size());
        for (MethodSummary m : list) {
            out.add(
                    new MethodSummary(
                            m.name(),
                            m.returnType(),
                            m.parameters() == null ? List.of() : m.parameters(),
                            m.methodCalls() == null ? List.of() : m.methodCalls(),
                            m.controlFlowStatements() == null ? List.of() : m.controlFlowStatements(),
                            m.fieldAccesses() == null ? List.of() : m.fieldAccesses(),
                            m.instantiations() == null ? List.of() : m.instantiations()));
        }
        return out;
    }

    public List<AstArtifact> artifacts() {
        return artifacts;
    }

    /** Placement order is stable (sorted by relative path + type name). */
    public List<PlacedArtifact> placedArtifacts() {
        return placedArtifacts;
    }

    public Set<String> projectTypeNames() {
        return projectTypeNames;
    }

    public Map<String, TypeSummary> typesBySimpleName() {
        return typesBySimpleName;
    }

    public TypeSummary typeOrNull(String simpleName) {
        return typesBySimpleName.get(simpleName);
    }

    public boolean isInterface(String simpleName) {
        TypeSummary t = typesBySimpleName.get(simpleName);
        return t != null && "interface".equals(t.kind());
    }

    /** Ordered iteration for deterministic DOT output. */
    public List<TypeSummary> typesInStableOrder() {
        return typesBySimpleName.values().stream().toList();
    }

    /**
     * @return method keys {@code Owner.method} or {@code Owner.<init>} present in the project model
     */
    public Set<String> projectMethodKeys() {
        return typesBySimpleName.entrySet().stream()
                .flatMap(
                        e -> e.getValue().methods().stream()
                                .map(m -> methodKey(e.getKey(), m)))
                .collect(Collectors.toCollection(java.util.TreeSet::new));
    }

    public static String methodKey(String ownerSimpleName, MethodSummary method) {
        Objects.requireNonNull(ownerSimpleName, "ownerSimpleName");
        Objects.requireNonNull(method, "method");
        if ("<init>".equals(method.returnType())) {
            return ownerSimpleName + ".<init>";
        }
        return ownerSimpleName + "." + method.name();
    }

    public static ParsedProject empty() {
        return new ParsedProject(List.of(), List.of(), Set.of(), Map.of());
    }
}

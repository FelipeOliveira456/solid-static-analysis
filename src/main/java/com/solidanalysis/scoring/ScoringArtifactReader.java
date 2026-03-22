package com.solidanalysis.scoring;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.algorithms.model.G1AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G2AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4FieldAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4ProjectionAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G5AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G3AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G6AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G7AlgorithmsDocument;
import com.solidanalysis.algorithms.runners.GraphFileMapper;
import com.solidanalysis.graphs.GraphObjectMapper;
import com.solidanalysis.graphs.io.ProjectJsonLoader;
import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.PlacedArtifact;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads scanner JSON, algorithm JSON, and parses DOT fragments needed for scoring (labels, G6
 * per client, G7 switch fan-out).
 */
public final class ScoringArtifactReader {

    private static final Pattern NODE =
            Pattern.compile("^\\s*(\\S+)\\s*\\[label=\"([^\"]*)\"(?:\\s*,[^\\]]*)?\\]\\s*;\\s*$");
    private static final Pattern EDGE =
            Pattern.compile("^\\s*(\\S+)\\s*->\\s*(\\S+)\\s*(?:\\[label=\"([^\"]*)\"\\])?\\s*;\\s*$");

    private final ObjectMapper mapper;

    public ScoringArtifactReader() {
        this.mapper = GraphObjectMapper.create();
    }

    public List<AstArtifact> loadAstArtifacts(Path projectOutputDir) throws IOException {
        return ProjectJsonLoader.loadArtifacts(projectOutputDir);
    }

    public List<PlacedArtifact> loadPlacedArtifacts(Path projectOutputDir) throws IOException {
        return ProjectJsonLoader.loadPlacedArtifacts(projectOutputDir);
    }

    public G1AlgorithmsDocument loadG1Algorithms(Path algorithmsDir) throws IOException {
        return mapper.readValue(algorithmsDir.resolve("g1_algorithms.json").toFile(), G1AlgorithmsDocument.class);
    }

    public G2AlgorithmsDocument loadG2Algorithms(Path algorithmsDir) throws IOException {
        return mapper.readValue(algorithmsDir.resolve("g2_algorithms.json").toFile(), G2AlgorithmsDocument.class);
    }

    public G5AlgorithmsDocument loadG5Algorithms(Path algorithmsDir) throws IOException {
        return mapper.readValue(algorithmsDir.resolve("g5_algorithms.json").toFile(), G5AlgorithmsDocument.class);
    }

    public G6AlgorithmsDocument loadG6Algorithms(Path algorithmsDir) throws IOException {
        return mapper.readValue(algorithmsDir.resolve("g6_algorithms.json").toFile(), G6AlgorithmsDocument.class);
    }

    public G4FieldAlgorithmsDocument loadG4Field(Path algorithmsDir, Path mirror, String className)
            throws IOException {
        Path p =
                algorithmsUnderMirror(algorithmsDir, mirror)
                        .resolve("g4_field_algorithms")
                        .resolve(className + ".json");
        return mapper.readValue(p.toFile(), G4FieldAlgorithmsDocument.class);
    }

    public G4ProjectionAlgorithmsDocument loadG4Projection(
            Path algorithmsDir, Path mirror, String className) throws IOException {
        Path p =
                algorithmsUnderMirror(algorithmsDir, mirror)
                        .resolve("g4_projection_algorithms")
                        .resolve(className + ".json");
        return mapper.readValue(p.toFile(), G4ProjectionAlgorithmsDocument.class);
    }

    public G3AlgorithmsDocument loadG3(Path algorithmsDir, Path mirror, String className)
            throws IOException {
        Path p =
                algorithmsUnderMirror(algorithmsDir, mirror)
                        .resolve("g3_algorithms")
                        .resolve(className + ".json");
        return mapper.readValue(p.toFile(), G3AlgorithmsDocument.class);
    }

    public G7AlgorithmsDocument loadG7(Path algorithmsDir, Path mirror, String baseName)
            throws IOException {
        Path p =
                algorithmsUnderMirror(algorithmsDir, mirror)
                        .resolve("g7_algorithms")
                        .resolve(baseName + ".json");
        return mapper.readValue(p.toFile(), G7AlgorithmsDocument.class);
    }

    private static Path algorithmsUnderMirror(Path algorithmsDir, Path mirror) {
        if (mirror == null || mirror.getNameCount() == 0) {
            return algorithmsDir;
        }
        return algorithmsDir.resolve(mirror);
    }

    /**
     * Count outgoing edges labeled {@code instantiation} from each class node in {@code
     * g1_dependency.dot}.
     */
    /** Distinct target node ids for all outgoing edges from {@code className} in G1. */
    public Set<String> parseG1OutgoingTargets(Path g1Dot, String className) throws IOException {
        Set<String> targets = new LinkedHashSet<>();
        for (String line : Files.readAllLines(g1Dot, StandardCharsets.UTF_8)) {
            Matcher m = EDGE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            String from = m.group(1);
            if (!className.equals(from)) {
                continue;
            }
            targets.add(m.group(2));
        }
        return targets;
    }

    public Map<String, Integer> parseG1InstantiationOutCounts(Path g1Dot) throws IOException {
        Map<String, Integer> counts = new HashMap<>();
        for (String line : Files.readAllLines(g1Dot, StandardCharsets.UTF_8)) {
            Matcher m = EDGE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            String from = m.group(1);
            String label = m.group(3);
            if (label != null && "instantiation".equals(label)) {
                counts.merge(from, 1, Integer::sum);
            }
        }
        return counts;
    }

    /** Child -> parent for edges labeled {@code extends} in {@code g2_inheritance.dot}. */
    public Map<String, String> parseG2Extends(Path g2Dot) throws IOException {
        Map<String, String> map = new HashMap<>();
        for (String line : Files.readAllLines(g2Dot, StandardCharsets.UTF_8)) {
            Matcher m = EDGE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            String from = m.group(1);
            String to = m.group(2);
            String label = m.group(3);
            if (label != null && "extends".equals(label)) {
                map.put(from, to);
            }
        }
        return map;
    }

    /** Interface type names ({@code shape=ellipse}) from {@code g5_interface_impl.dot}. */
    public Set<String> parseG5EllipseInterfaces(Path g5Dot) throws IOException {
        Set<String> ids = new HashSet<>();
        for (String line : Files.readAllLines(g5Dot, StandardCharsets.UTF_8)) {
            String t = line.trim();
            if (t.contains("shape=ellipse") && t.contains("[") && !t.contains("->")) {
                int lb = t.indexOf('[');
                String head = t.substring(0, lb).trim();
                if (!head.isEmpty()) {
                    ids.add(head.split("\\s+")[0]);
                }
            }
        }
        return ids;
    }

    /**
     * For each client class, interfaces used as types (outgoing edges to known interface ids) from
     * {@code g6_interface_usage.dot}.
     */
    public Map<String, Set<String>> parseG6ClientInterfaceUsage(Path g6Dot, Set<String> interfaceIds)
            throws IOException {
        Map<String, Set<String>> out = new LinkedHashMap<>();
        for (String line : Files.readAllLines(g6Dot, StandardCharsets.UTF_8)) {
            Matcher m = EDGE.matcher(line.trim());
            if (!m.matches()) {
                continue;
            }
            String from = m.group(1);
            String to = m.group(2);
            if (interfaceIds.contains(to)) {
                out.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
            }
        }
        return out;
    }

    /**
     * Maximum out-degree from any CFG node whose label contains {@code switch} (case branches) in
     * a G7 {@code .dot} file.
     */
    public int parseG7MaxSwitchFanout(Path g7Dot) throws IOException {
        Map<String, String> nodeLabels = new HashMap<>();
        List<String[]> edges = new ArrayList<>();
        for (String line : Files.readAllLines(g7Dot, StandardCharsets.UTF_8)) {
            String t = line.trim();
            Matcher nm = NODE.matcher(t);
            if (nm.matches()) {
                nodeLabels.put(nm.group(1), nm.group(2));
                continue;
            }
            Matcher em = EDGE.matcher(t);
            if (em.matches()) {
                edges.add(new String[] {em.group(1), em.group(2), em.group(3)});
            }
        }
        int max = 0;
        for (Map.Entry<String, String> e : nodeLabels.entrySet()) {
            String label = e.getValue().toLowerCase(Locale.ROOT);
            if (!label.contains("switch")) {
                continue;
            }
            String id = e.getKey();
            int out = 0;
            for (String[] edge : edges) {
                if (id.equals(edge[0])) {
                    out++;
                }
            }
            max = Math.max(max, out);
        }
        return max;
    }

    /**
     * All G7 CFG {@code .dot} files under {@code graphs/} (any {@code .../g7_cfg/*.dot}), with mirror
     * path relative to {@code graphs/}.
     */
    public List<GraphFileMapper.MirroredDot> listG7MirroredDots(Path projectOutputDir)
            throws IOException {
        return new GraphFileMapper(projectOutputDir).listMirroredLayerDots("g7_cfg");
    }

    public static String classNameFromG7Base(String base) {
        int us = base.indexOf('_');
        if (us <= 0) {
            return base;
        }
        return base.substring(0, us);
    }

    public static String methodNameFromG7Base(String base) {
        int us = base.indexOf('_');
        if (us <= 0 || us >= base.length() - 1) {
            return base;
        }
        return base.substring(us + 1);
    }

    public static Map<String, Boolean> abstractByClass(List<AstArtifact> artifacts) {
        Map<String, Boolean> m = new LinkedHashMap<>();
        for (AstArtifact a : artifacts) {
            if (a.primaryType() != null && a.primaryType().name() != null) {
                m.put(a.primaryType().name(), a.primaryType().abstractType());
            }
        }
        return m;
    }

    public static Map<String, String> superclassByClass(List<AstArtifact> artifacts) {
        Map<String, String> m = new LinkedHashMap<>();
        for (AstArtifact a : artifacts) {
            if (a.primaryType() != null && a.primaryType().name() != null) {
                m.put(a.primaryType().name(), a.primaryType().superclass());
            }
        }
        return m;
    }

    public static List<String> classNamesSorted(List<AstArtifact> artifacts) {
        List<String> names = new ArrayList<>();
        for (AstArtifact a : artifacts) {
            if (a.primaryType() != null && a.primaryType().name() != null) {
                names.add(a.primaryType().name());
            }
        }
        Collections.sort(names);
        return names;
    }
}

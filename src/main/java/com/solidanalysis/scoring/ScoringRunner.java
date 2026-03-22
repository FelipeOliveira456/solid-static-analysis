package com.solidanalysis.scoring;

import com.solidanalysis.algorithms.model.G1AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G2AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G3AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4FieldAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G4ProjectionAlgorithmsDocument;
import com.solidanalysis.algorithms.model.G5AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G6AlgorithmsDocument;
import com.solidanalysis.algorithms.model.G7AlgorithmsDocument;
import com.solidanalysis.algorithms.runners.GraphFileMapper;
import com.solidanalysis.graphs.io.ProjectOutputLayout;
import com.solidanalysis.graphs.model.PlacedArtifact;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Computes SOLID scores from Stage 1–3 artifacts under a project output directory and writes
 * {@code scoring/} JSON files.
 */
public final class ScoringRunner {

    private record ScoredInd(IndicatorResult result, ScoreLevel level) {}

    public boolean run(Path projectOutputDir, Path repoRoot, PrintStream err) {
        try {
            runInternal(projectOutputDir, repoRoot);
            return true;
        } catch (Exception e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return false;
        }
    }

    private void runInternal(Path projectOutputDir, Path repoRoot) throws IOException {
        Path algorithms = projectOutputDir.resolve("algorithms");
        Path graphs = projectOutputDir.resolve("graphs");
        Path g1Dot = graphs.resolve("g1_dependency.dot");
        requireDir(algorithms, "Missing algorithms directory: ");
        requireDir(graphs, "Missing graphs directory: ");
        requireFile(algorithms.resolve("g1_algorithms.json"));
        requireFile(algorithms.resolve("g2_algorithms.json"));
        requireFile(algorithms.resolve("g5_algorithms.json"));
        requireFile(algorithms.resolve("g6_algorithms.json"));
        requireFile(g1Dot);
        requireFile(graphs.resolve("g2_inheritance.dot"));
        requireFile(graphs.resolve("g5_interface_impl.dot"));
        requireFile(graphs.resolve("g6_interface_usage.dot"));
        ScoringArtifactReader reader = new ScoringArtifactReader();
        List<PlacedArtifact> placed = new ArrayList<>(reader.loadPlacedArtifacts(projectOutputDir));
        placed.removeIf(pa -> pa.simpleTypeName().isEmpty());
        placed.sort(Comparator.comparing(PlacedArtifact::slotKey));
        if (placed.isEmpty()) {
            throw new IOException(
                    "No AST JSON under "
                            + ProjectOutputLayout.astDirectory(projectOutputDir).toAbsolutePath()
                            + " (run Etapa 1 scan into ast/)");
        }
        List<GraphFileMapper.MirroredDot> g7All = reader.listG7MirroredDots(projectOutputDir);
        for (PlacedArtifact pa : placed) {
            Path mirror = pa.relativeOutputDir();
            String cn = pa.simpleTypeName();
            requireFile(algorithmsUnderMirror(algorithms, mirror).resolve("g4_field_algorithms").resolve(cn + ".json"));
            requireFile(
                    algorithmsUnderMirror(algorithms, mirror)
                            .resolve("g4_projection_algorithms")
                            .resolve(cn + ".json"));
            requireFile(algorithmsUnderMirror(algorithms, mirror).resolve("g3_algorithms").resolve(cn + ".json"));
        }

        ThresholdConfiguration thresholds = ThresholdConfiguration.load(repoRoot);
        int n = placed.size();
        ScoringStrategy strategy =
                n < 10 ? ScoringStrategy.FIXED_THRESHOLD_RELAXED : ScoringStrategy.Z_SCORE;
        double relaxF = RelaxedFixedClassification.relaxFactor(n, thresholds.relaxK());
        ClassificationService fixed = new ClassificationService(thresholds, strategy);

        G1AlgorithmsDocument g1 = reader.loadG1Algorithms(algorithms);
        G2AlgorithmsDocument g2 = reader.loadG2Algorithms(algorithms);
        G5AlgorithmsDocument g5 = reader.loadG5Algorithms(algorithms);
        G6AlgorithmsDocument g6 = reader.loadG6Algorithms(algorithms);
        Map<String, Set<String>> g5IfaceImplementors =
                reader.parseG5InterfaceImplementors(graphs.resolve("g5_interface_impl.dot"));
        Map<String, Integer> instOut = reader.parseG1InstantiationOutCounts(g1Dot);
        Map<String, String> extendsMap = reader.parseG2Extends(graphs.resolve("g2_inheritance.dot"));
        Set<String> g5IfaceNodes = reader.parseG5EllipseInterfaces(graphs.resolve("g5_interface_impl.dot"));
        Set<String> knownIfaces = new HashSet<>();
        if (g6.knownInterfacesFromG5 != null) {
            knownIfaces.addAll(g6.knownInterfacesFromG5);
        }
        knownIfaces.addAll(g5IfaceNodes);

        Map<String, Boolean> abstractBy = abstractByClassFromPlaced(placed);
        int g1NodeCount = g1.outDegree == null ? 0 : g1.outDegree.size();

        Map<String, List<ScoredInd>> sMap = newMapsForPlaced(placed);
        Map<String, List<ScoredInd>> oMap = newMapsForPlaced(placed);
        Map<String, List<ScoredInd>> lMap = newMapsForPlaced(placed);
        Map<String, List<ScoredInd>> iMap = newMapsForPlaced(placed);
        Map<String, List<ScoredInd>> dMap = newMapsForPlaced(placed);

        Map<String, Double> lcomV = new LinkedHashMap<>();
        Map<String, Double> isoV = new LinkedHashMap<>();
        Map<String, Double> projV = new LinkedHashMap<>();
        Map<String, Double> swV = new LinkedHashMap<>();
        Map<String, Double> depthV = new LinkedHashMap<>();
        Map<String, Double> ciV = new LinkedHashMap<>();
        Map<String, Double> subV = new LinkedHashMap<>();
        Map<String, Double> implV = new LinkedHashMap<>();
        Map<String, Double> instV = new LinkedHashMap<>();
        Map<String, Double> onormV = new LinkedHashMap<>();
        Map<String, Double> cdrV = new LinkedHashMap<>();
        Map<String, Double> ocentV = new LinkedHashMap<>();

        Map<String, Integer> subclassCount = subclassCounts(extendsMap, abstractBy);

        for (PlacedArtifact pa : placed) {
            Path mirror = pa.relativeOutputDir();
            String cn = pa.simpleTypeName();
            String sk = pa.slotKey();
            G4FieldAlgorithmsDocument g4f = reader.loadG4Field(algorithms, mirror, cn);
            G4ProjectionAlgorithmsDocument g4p = reader.loadG4Projection(algorithms, mirror, cn);
            G3AlgorithmsDocument g3 = reader.loadG3(algorithms, mirror, cn);

            double lcom = g4f.lcom;
            lcomV.put(sk, lcom);
            int pctLcom = (int) Math.round(lcom * 100.0);
            add(
                    sMap,
                    sk,
                    IndicatorTemplate.LCOM_VALUE,
                    lcom,
                    IndicatorTemplate.formatLcomValue(lcom, pctLcom),
                    classifyLcom(lcom, strategy, fixed, lcomV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            int pc = g4p.clusters == null ? 0 : g4p.clusters.size();
            projV.put(sk, (double) pc);
            add(
                    sMap,
                    sk,
                    IndicatorTemplate.PROJECTION_CLUSTERS,
                    pc,
                    IndicatorTemplate.formatProjectionClusters(pc),
                    classifyProj(pc, strategy, fixed, projV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            int isoN = g3.isolatedNodes == null ? 0 : g3.isolatedNodes.size();
            int inDegKeys = g3.inDegree == null ? 0 : g3.inDegree.size();
            double isoRatio = inDegKeys == 0 ? 0.0 : (double) isoN / inDegKeys;
            isoV.put(sk, isoRatio);
            int isoPct = inDegKeys == 0 ? 0 : (int) Math.round(100.0 * isoN / inDegKeys);
            add(
                    sMap,
                    sk,
                    IndicatorTemplate.ISOLATED_METHODS_RATIO,
                    isoRatio,
                    IndicatorTemplate.formatIsolatedMethodsRatio(isoN, inDegKeys, isoPct),
                    classifyIso(isoRatio, strategy, fixed, isoV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            if (g3.stronglyConnectedComponents != null) {
                for (List<String> comp : g3.stronglyConnectedComponents) {
                    if (comp != null && comp.size() > 1) {
                        add(
                                sMap,
                                sk,
                                IndicatorTemplate.G3_SCC_CYCLE,
                                comp.size(),
                                IndicatorTemplate.formatG3SccCycle(comp),
                                ScoreLevel.ALTO,
                                strategy,
                                relaxF);
                        break;
                    }
                }
            }

            int swMax = 0;
            String swMethod = "";
            for (GraphFileMapper.MirroredDot md : g7All) {
                if (!md.mirrorRelativeToGraphs().normalize().equals(mirror.normalize())) {
                    continue;
                }
                String base = md.fileStem();
                if (!ScoringArtifactReader.classNameFromG7Base(base).equals(cn)) {
                    continue;
                }
                Path dot = md.dotFile();
                int fan = reader.parseG7MaxSwitchFanout(dot);
                if (fan <= 0) {
                    continue;
                }
                G7AlgorithmsDocument g7j = reader.loadG7(algorithms, mirror, base);
                if (g7j.maxDecisionOutDegree <= 0) {
                    continue;
                }
                if (fan > swMax) {
                    swMax = fan;
                    swMethod = ScoringArtifactReader.methodNameFromG7Base(base);
                }
            }
            swV.put(sk, (double) swMax);
            if (swMax > 0) {
                add(
                        oMap,
                        sk,
                        IndicatorTemplate.SWITCH_CASES,
                        swMax,
                        IndicatorTemplate.formatSwitchCases(swMax, swMethod),
                        classifySwitch(swMax, strategy, fixed, swV, sk),
                        strategy,
                        relaxF);
            }

            String parent = extendsMap.get(cn);
            if (parent != null
                    && !Boolean.TRUE.equals(abstractBy.get(parent))
                    && !Boolean.TRUE.equals(abstractBy.get(cn))) {
                add(
                        oMap,
                        sk,
                        IndicatorTemplate.EXTENDS_CONCRETE,
                        1,
                        IndicatorTemplate.formatExtendsConcrete(parent),
                        ScoreLevel.ALTO,
                        strategy,
                        relaxF);
            }

            int depth =
                    g2.maxDepthFromRootEdges == null ? 0 : g2.maxDepthFromRootEdges.getOrDefault(cn, 0);
            depthV.put(sk, (double) depth);
            add(
                    lMap,
                    sk,
                    IndicatorTemplate.INHERITANCE_DEPTH,
                    depth,
                    IndicatorTemplate.formatInheritanceDepth(depth, cn),
                    classifyDepth(depth, strategy, fixed, depthV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            if (!Boolean.TRUE.equals(abstractBy.get(cn))) {
                int indeg = g2.inDegree == null ? 0 : g2.inDegree.getOrDefault(cn, 0);
                ciV.put(sk, (double) indeg);
                add(
                        lMap,
                        sk,
                        IndicatorTemplate.CONCRETE_CLASS_INDEGREE,
                        indeg,
                        IndicatorTemplate.formatConcreteClassIndegree(indeg),
                        classifyConcIndeg(indeg, strategy, fixed, ciV, sk, relaxF, thresholds),
                        strategy,
                        relaxF);
            }

            int subN = subclassCount.getOrDefault(cn, 0);
            subV.put(sk, (double) subN);
            if (subN > 0 && !Boolean.TRUE.equals(abstractBy.get(cn))) {
                add(
                        lMap,
                        sk,
                        IndicatorTemplate.CONCRETE_SUBCLASS_COUNT,
                        subN,
                        IndicatorTemplate.formatConcreteSubclassCount(subN, cn),
                        classifyConcIndeg(subN, strategy, fixed, subV, sk, relaxF, thresholds),
                        strategy,
                        relaxF);
            }

            int impl =
                    g5.classOutDegree == null ? 0 : g5.classOutDegree.getOrDefault(cn, 0);
            implV.put(sk, (double) impl);
            add(
                    iMap,
                    sk,
                    IndicatorTemplate.IMPLEMENTS_COUNT,
                    impl,
                    IndicatorTemplate.formatImplementsCount(impl),
                    classifyImpl(impl, strategy, fixed, implV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            int inst = instOut.getOrDefault(cn, 0);
            instV.put(sk, (double) inst);
            List<String> instTargets = instantiationTargets(g1Dot, cn);
            add(
                    dMap,
                    sk,
                    IndicatorTemplate.INSTANTIATION_COUNT,
                    inst,
                    IndicatorTemplate.formatInstantiationCount(inst, instTargets),
                    classifyInst(inst, strategy, fixed, instV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            int od = g1.outDegree == null ? 0 : g1.outDegree.getOrDefault(cn, 0);
            double onorm = g1NodeCount == 0 ? 0.0 : (double) od / g1NodeCount;
            onormV.put(sk, onorm);
            int onPct = g1NodeCount == 0 ? 0 : (int) Math.round(100.0 * od / g1NodeCount);
            add(
                    dMap,
                    sk,
                    IndicatorTemplate.OUT_DEGREE_NORMALIZED,
                    onorm,
                    IndicatorTemplate.formatOutDegreeNormalized(od, g1NodeCount, onPct),
                    classifyOnorm(onorm, strategy, fixed, onormV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            int[] cdc = concreteDepCounts(reader, g1Dot, cn, knownIfaces);
            double cdr = cdc[1] == 0 ? 0.0 : (double) cdc[0] / cdc[1];
            cdrV.put(sk, cdr);
            int cPct = cdc[1] == 0 ? 0 : (int) Math.round(100.0 * cdc[0] / cdc[1]);
            add(
                    dMap,
                    sk,
                    IndicatorTemplate.CONCRETE_DEPENDENCY_RATIO,
                    cdr,
                    IndicatorTemplate.formatConcreteDependencyRatio(cPct, cdc[0], cdc[1]),
                    classifyCdr(cdr, strategy, fixed, cdrV, sk, relaxF, thresholds),
                    strategy,
                    relaxF);

            double oc =
                    g1.outCentrality == null ? 0.0 : g1.outCentrality.getOrDefault(cn, 0.0);
            ocentV.put(sk, oc);
            add(
                    dMap,
                    sk,
                    IndicatorTemplate.G1_OUT_CENTRALITY,
                    oc,
                    IndicatorTemplate.formatG1OutCentrality(oc),
                    classifyCentrality(oc, strategy, fixed, ocentV, sk, thresholds),
                    strategy,
                    relaxF);

            if (g1.stronglyConnectedComponents != null) {
                for (List<String> comp : g1.stronglyConnectedComponents) {
                    if (comp != null && comp.contains(cn) && comp.size() > 1) {
                        add(
                                dMap,
                                sk,
                                IndicatorTemplate.G1_CYCLE,
                                comp.size(),
                                IndicatorTemplate.formatG1Cycle(comp),
                                ScoreLevel.ALTO,
                                strategy,
                                relaxF);
                        break;
                    }
                }
            }
        }

        if (g5.interfacesWithZeroInDegree != null) {
            for (String iface : g5.interfacesWithZeroInDegree) {
                for (String sk : slotKeysForSimpleNames(Set.of(iface), placed)) {
                    add(
                            iMap,
                            sk,
                            IndicatorTemplate.INTERFACE_ZERO_INDEGREE_IMPL,
                            iface,
                            IndicatorTemplate.formatInterfaceZeroIndegreeImpl(iface),
                            ScoreLevel.ALTO,
                            strategy,
                            relaxF);
                }
            }
        }
        if (g6.interfacesWithZeroInDegree != null) {
            for (String iface : g6.interfacesWithZeroInDegree) {
                Set<String> involved = new LinkedHashSet<>();
                involved.add(iface);
                involved.addAll(g5IfaceImplementors.getOrDefault(iface, Collections.emptySet()));
                for (String sk : slotKeysForSimpleNames(involved, placed)) {
                    add(
                            iMap,
                            sk,
                            IndicatorTemplate.INTERFACE_ZERO_INDEGREE_USAGE,
                            iface,
                            IndicatorTemplate.formatInterfaceZeroIndegreeUsage(iface),
                            ScoreLevel.ALTO,
                            strategy,
                            relaxF);
                }
            }
        }

        String projectPath = projectOutputDir.toAbsolutePath().normalize().toString();
        List<ClassScore> outClasses = new ArrayList<>();
        for (PlacedArtifact pa : placed) {
            String sk = pa.slotKey();
            String cn = pa.simpleTypeName();
            String rel = mirrorToString(pa.relativeOutputDir());
            Map<String, PrincipleScore> pmap = new LinkedHashMap<>();
            ScoreLevel overall = ScoreLevel.BAIXO;
            overall = ScoreLevel.worst(overall, finishPrinciple(pmap, "S", sMap.get(sk), n));
            overall = ScoreLevel.worst(overall, finishPrinciple(pmap, "O", oMap.get(sk), n));
            overall = ScoreLevel.worst(overall, finishPrinciple(pmap, "L", lMap.get(sk), n));
            overall = ScoreLevel.worst(overall, finishPrinciple(pmap, "I", iMap.get(sk), n));
            overall = ScoreLevel.worst(overall, finishPrinciple(pmap, "D", dMap.get(sk), n));
            outClasses.add(new ClassScore(cn, rel, projectPath, strategy, relaxF, pmap, overall));
        }

        Path scoringDir = projectOutputDir.resolve("scoring");
        ScoringReportWriter writer = new ScoringReportWriter();
        for (ClassScore cs : outClasses) {
            writer.writeClassScore(scoringDir, cs);
        }
        ProjectSummary summary =
                ProjectSummaryBuilder.build(projectPath, strategy, relaxF, outClasses);
        writer.writeProjectSummary(scoringDir, summary);

        Path resultsDir = projectOutputDir.resolve("results");
        HumanReadableResultsWriter humanWriter = new HumanReadableResultsWriter();
        humanWriter.writeClassReports(resultsDir, outClasses);
        humanWriter.writeProjectSummary(resultsDir, summary);
    }

    private static Map<String, List<ScoredInd>> newMapsForPlaced(List<PlacedArtifact> placed) {
        Map<String, List<ScoredInd>> m = new LinkedHashMap<>();
        for (PlacedArtifact pa : placed) {
            m.put(pa.slotKey(), new ArrayList<>());
        }
        return m;
    }

    /** Slot keys for every placed artifact whose primary type simple name is in {@code simpleNames}. */
    private static Set<String> slotKeysForSimpleNames(
            Collection<String> simpleNames, List<PlacedArtifact> placed) {
        if (simpleNames.isEmpty()) {
            return Set.of();
        }
        Set<String> want = new HashSet<>(simpleNames);
        Set<String> keys = new LinkedHashSet<>();
        for (PlacedArtifact pa : placed) {
            String sn = pa.simpleTypeName();
            if (!sn.isEmpty() && want.contains(sn)) {
                keys.add(pa.slotKey());
            }
        }
        return keys;
    }

    private static Path algorithmsUnderMirror(Path algorithms, Path mirror) {
        if (mirror == null || mirror.getNameCount() == 0) {
            return algorithms;
        }
        return algorithms.resolve(mirror);
    }

    private static String mirrorToString(Path mirror) {
        if (mirror == null || mirror.getNameCount() == 0) {
            return "";
        }
        return mirror.toString().replace('\\', '/');
    }

    private static Map<String, Boolean> abstractByClassFromPlaced(List<PlacedArtifact> placed) {
        Map<String, Boolean> m = new LinkedHashMap<>();
        for (PlacedArtifact pa : placed) {
            var pt = pa.artifact().primaryType();
            if (pt != null && pt.name() != null) {
                m.put(pt.name(), pt.abstractType());
            }
        }
        return m;
    }

    private static void add(
            Map<String, List<ScoredInd>> map,
            String slotKey,
            IndicatorTemplate t,
            Object rawMetric,
            String detail,
            ScoreLevel level,
            ScoringStrategy strategy,
            double relaxF) {
        Object jsonValue = RelaxationValueScaling.scaledValueForJson(t, rawMetric, strategy, relaxF);
        map.get(slotKey)
                .add(new ScoredInd(new IndicatorResult(t, rawMetric, jsonValue, detail), level));
    }

    private static ScoreLevel finishPrinciple(
            Map<String, PrincipleScore> pmap, String letter, List<ScoredInd> scored, int nClasses) {
        List<ScoredInd> adjusted = applySmallProjectPairingRule(scored, nClasses);
        if (adjusted.isEmpty()) {
            pmap.put(letter, new PrincipleScore(letter, ScoreLevel.BAIXO, List.of()));
            return ScoreLevel.BAIXO;
        }
        ScoreLevel worst = ScoreLevel.BAIXO;
        List<IndicatorResult> kept = new ArrayList<>();
        for (ScoredInd si : adjusted) {
            worst = ScoreLevel.worst(worst, si.level);
            kept.add(si.result);
        }
        pmap.put(letter, new PrincipleScore(letter, worst, kept));
        return worst;
    }

    /**
     * For n&lt;5 classes: a single “pairing continuous” ALTO is downgraded to MEDIO unless a
     * structural ALTO is present in the same principle.
     */
    private static List<ScoredInd> applySmallProjectPairingRule(List<ScoredInd> scored, int n) {
        if (n >= 5) {
            return scored;
        }
        boolean structuralAlto =
                scored.stream()
                        .anyMatch(
                                si ->
                                        IndicatorPolicies.isStructuralIndicator(
                                                        si.result.templateId())
                                                && si.level == ScoreLevel.ALTO);
        if (structuralAlto) {
            return scored;
        }
        long pairingAlto =
                scored.stream()
                        .filter(
                                si ->
                                        IndicatorPolicies.isSmallProjectPairingContinuous(
                                                        si.result.templateId())
                                                && si.level == ScoreLevel.ALTO)
                        .count();
        if (pairingAlto != 1) {
            return scored;
        }
        List<ScoredInd> out = new ArrayList<>();
        for (ScoredInd si : scored) {
            if (IndicatorPolicies.isSmallProjectPairingContinuous(si.result.templateId())
                    && si.level == ScoreLevel.ALTO) {
                out.add(new ScoredInd(si.result, ScoreLevel.MEDIO));
            } else {
                out.add(si);
            }
        }
        return out;
    }

    private static void requireDir(Path p, String msg) throws IOException {
        if (!Files.isDirectory(p)) {
            throw new IOException(msg + p.toAbsolutePath());
        }
    }

    private static void requireFile(Path p) throws IOException {
        if (!Files.isRegularFile(p)) {
            throw new IOException("Missing required file: " + p.toAbsolutePath());
        }
    }

    private static Map<String, Integer> subclassCounts(
            Map<String, String> extendsMap, Map<String, Boolean> abstractBy) {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, String> e : extendsMap.entrySet()) {
            String parent = e.getValue();
            if (parent != null && !Boolean.TRUE.equals(abstractBy.get(parent))) {
                counts.merge(parent, 1, Integer::sum);
            }
        }
        return counts;
    }

    private static List<String> instantiationTargets(Path g1Dot, String fromClass) throws IOException {
        List<String> targets = new ArrayList<>();
        ScoringArtifactReader r = new ScoringArtifactReader();
        Map<String, Integer> m = r.parseG1InstantiationOutCounts(g1Dot);
        if (m.getOrDefault(fromClass, 0) == 0) {
            return targets;
        }
        for (String line : Files.readAllLines(g1Dot)) {
            String t = line.trim();
            if (!t.contains("->") || !t.contains("instantiation")) {
                continue;
            }
            int arr = t.indexOf("->");
            String from = t.substring(0, arr).trim().split("\\s+")[0];
            if (!fromClass.equals(from)) {
                continue;
            }
            String rest = t.substring(arr + 2).trim();
            String to = rest.split("\\s+")[0];
            if (to.endsWith("[")) {
                continue;
            }
            if (to.endsWith(";")) {
                to = to.substring(0, to.length() - 1).trim();
            }
            targets.add(to);
        }
        return targets;
    }

    private static int[] concreteDepCounts(
            ScoringArtifactReader reader, Path g1Dot, String cn, Set<String> ifaces)
            throws IOException {
        Set<String> targets = reader.parseG1OutgoingTargets(g1Dot, cn);
        int total = targets.size();
        int conc = 0;
        for (String t : targets) {
            if (!ifaces.contains(t)) {
                conc++;
            }
        }
        return new int[] {conc, total};
    }

    private static ScoreLevel classifyLcom(
            double v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify(v, all, cn);
        }
        return RelaxedFixedClassification.classifyLcom(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyProj(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return RelaxedFixedClassification.classifyProjectionClusters(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyIso(
            double v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify(v, all, cn);
        }
        return RelaxedFixedClassification.classifyIsolatedRatio(v, relaxF, thresholds);
    }

    private static ScoreLevel classifySwitch(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return fixed.classifySwitchCases(v);
    }

    private static ScoreLevel classifyDepth(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return RelaxedFixedClassification.classifyDepth(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyConcIndeg(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return RelaxedFixedClassification.classifyConcreteIndegree(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyImpl(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return RelaxedFixedClassification.classifyImplementsCount(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyInst(
            int v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify((double) v, all, cn);
        }
        return RelaxedFixedClassification.classifyInstantiations(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyOnorm(
            double v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify(v, all, cn);
        }
        return RelaxedFixedClassification.classifyOutDegreeNormalized(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyCdr(
            double v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            double relaxF,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify(v, all, cn);
        }
        return RelaxedFixedClassification.classifyConcreteDependencyRatio(v, relaxF, thresholds);
    }

    private static ScoreLevel classifyCentrality(
            double v,
            ScoringStrategy st,
            ClassificationService fixed,
            Map<String, Double> all,
            String cn,
            ThresholdConfiguration thresholds) {
        if (st == ScoringStrategy.Z_SCORE) {
            return zClassify(v, all, cn);
        }
        if (v >= thresholds.g1OutCentralityAlto()) {
            return ScoreLevel.ALTO;
        }
        if (v >= thresholds.g1OutCentralityMedio()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.BAIXO;
    }

    private static ScoreLevel zClassify(double value, Map<String, Double> perClass, String cn) {
        List<Double> vals = new ArrayList<>(perClass.values());
        double mean = ClassificationService.mean(vals);
        double sig = ClassificationService.sigmaPop(vals, mean);
        return ClassificationService.classifyZScore(value, mean, sig);
    }
}

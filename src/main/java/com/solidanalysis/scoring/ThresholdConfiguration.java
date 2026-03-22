package com.solidanalysis.scoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Thresholds for fixed-threshold and relaxed small-project scoring. Values are read from the bundled
 * {@code /analysis.properties} on the classpath (same content as the repo-root file), then merged
 * with optional overrides from {@code repoRoot/analysis.properties} when using {@link #load(Path)}.
 *
 * <p>Small-project relaxation uses {@code scoring.relax.k} in {@code f(n)=n/(n+k)} ({@link
 * ScoringStrategy#FIXED_THRESHOLD_RELAXED}).
 */
public final class ThresholdConfiguration {

    private final double lcomMedio;
    private final double lcomAlto;
    private final int projectionMedio;
    private final int projectionAlto;
    private final double isolatedMedio;
    private final double isolatedAlto;
    private final int switchMedio;
    private final int switchAlto;
    private final int depthMedio;
    private final int depthAlto;
    private final int indegreeMedio;
    private final int indegreeAlto;
    private final int implMedio;
    private final int implAlto;
    private final int instMedio;
    private final int instAlto;
    private final double outNormMedio;
    private final double outNormAlto;
    private final double concreteRatioMedio;
    private final double concreteRatioAlto;
    private final double g1OutCentralityMedio;
    private final double g1OutCentralityAlto;
    private final int relaxK;
    /** {@code k} in {@code C(n,2)/(C(n,2)+k)} for isolated-methods weighting (G3). */
    private final double isolatedMethodsCombinationsK;
    /**
     * H1 for {@link DispatchAstHeuristicAnalyzer}: fire when homogeneity {@code H = 1 - d/n} is
     * strictly greater than this value (0–1).
     */
    private final double dispatchAstH1HomogeneityThreshold;
    private final int dispatchAstH2MinChainIfs;
    private final int dispatchAstH2MinConsecutiveTopIfs;

    private ThresholdConfiguration(Builder b) {
        this.lcomMedio = b.lcomMedio;
        this.lcomAlto = b.lcomAlto;
        this.projectionMedio = b.projectionMedio;
        this.projectionAlto = b.projectionAlto;
        this.isolatedMedio = b.isolatedMedio;
        this.isolatedAlto = b.isolatedAlto;
        this.switchMedio = b.switchMedio;
        this.switchAlto = b.switchAlto;
        this.depthMedio = b.depthMedio;
        this.depthAlto = b.depthAlto;
        this.indegreeMedio = b.indegreeMedio;
        this.indegreeAlto = b.indegreeAlto;
        this.implMedio = b.implMedio;
        this.implAlto = b.implAlto;
        this.instMedio = b.instMedio;
        this.instAlto = b.instAlto;
        this.outNormMedio = b.outNormMedio;
        this.outNormAlto = b.outNormAlto;
        this.concreteRatioMedio = b.concreteRatioMedio;
        this.concreteRatioAlto = b.concreteRatioAlto;
        this.g1OutCentralityMedio = b.g1OutCentralityMedio;
        this.g1OutCentralityAlto = b.g1OutCentralityAlto;
        this.relaxK = b.relaxK;
        this.isolatedMethodsCombinationsK = b.isolatedMethodsCombinationsK;
        this.dispatchAstH1HomogeneityThreshold = b.dispatchAstH1HomogeneityThreshold;
        this.dispatchAstH2MinChainIfs = b.dispatchAstH2MinChainIfs;
        this.dispatchAstH2MinConsecutiveTopIfs = b.dispatchAstH2MinConsecutiveTopIfs;
    }

    /** Loads bundled {@code analysis.properties} from the classpath (no workspace override). */
    public static ThresholdConfiguration defaults() {
        try {
            return fromProperties(loadBundledProperties());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Merges bundled defaults with {@code repoRoot/analysis.properties} when that file exists
     * (override wins).
     */
    public static ThresholdConfiguration load(Path repoRoot) throws IOException {
        Objects.requireNonNull(repoRoot, "repoRoot");
        Properties p = loadBundledProperties();
        Path propsPath = repoRoot.resolve("analysis.properties");
        if (Files.isRegularFile(propsPath)) {
            try (Reader r = Files.newBufferedReader(propsPath)) {
                Properties over = new Properties();
                over.load(r);
                p.putAll(over);
            }
        }
        return fromProperties(p);
    }

    private static Properties loadBundledProperties() throws IOException {
        try (InputStream in =
                ThresholdConfiguration.class.getResourceAsStream("/analysis.properties")) {
            if (in == null) {
                throw new IOException("Missing classpath resource /analysis.properties");
            }
            Properties p = new Properties();
            try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                p.load(r);
            }
            return p;
        }
    }

    private static ThresholdConfiguration fromProperties(Properties p) {
        Builder b = new Builder();
        b.lcomMedio = reqDouble(p, "threshold.lcom.medio");
        b.lcomAlto = reqDouble(p, "threshold.lcom.alto");
        b.projectionMedio = reqInt(p, "threshold.projectionClusters.medio");
        b.projectionAlto = reqInt(p, "threshold.projectionClusters.alto");
        b.isolatedMedio = reqDouble(p, "threshold.isolatedRatio.medio");
        b.isolatedAlto = reqDouble(p, "threshold.isolatedRatio.alto");
        b.switchMedio = reqInt(p, "threshold.switchCases.medio");
        b.switchAlto = reqInt(p, "threshold.switchCases.alto");
        b.depthMedio = reqInt(p, "threshold.inheritanceDepth.medio");
        b.depthAlto = reqInt(p, "threshold.inheritanceDepth.alto");
        b.indegreeMedio = reqInt(p, "threshold.concreteIndegree.medio");
        b.indegreeAlto = reqInt(p, "threshold.concreteIndegree.alto");
        b.implMedio = reqInt(p, "threshold.implementsCount.medio");
        b.implAlto = reqInt(p, "threshold.implementsCount.alto");
        b.instMedio = reqInt(p, "threshold.instantiations.medio");
        b.instAlto = reqInt(p, "threshold.instantiations.alto");
        b.outNormMedio = reqDouble(p, "threshold.outDegreeNormalized.medio");
        b.outNormAlto = reqDouble(p, "threshold.outDegreeNormalized.alto");
        b.concreteRatioMedio = reqDouble(p, "threshold.concreteDependencyRatio.medio");
        b.concreteRatioAlto = reqDouble(p, "threshold.concreteDependencyRatio.alto");
        b.g1OutCentralityMedio = reqDouble(p, "threshold.g1OutCentrality.medio");
        b.g1OutCentralityAlto = reqDouble(p, "threshold.g1OutCentrality.alto");
        b.relaxK = reqInt(p, "scoring.relax.k");
        b.isolatedMethodsCombinationsK = reqNonNegativeDouble(p, "scoring.isolatedMethods.combinations.k");
        b.dispatchAstH1HomogeneityThreshold =
                reqFractionExclusiveUpper(p, "scoring.dispatchAst.h1.homogeneityThreshold");
        b.dispatchAstH2MinChainIfs =
                reqIntAtLeast(p, "scoring.dispatchAst.h2.minChainIfs", 2);
        b.dispatchAstH2MinConsecutiveTopIfs =
                reqIntAtLeast(p, "scoring.dispatchAst.h2.minConsecutiveTopIfs", 2);
        return b.build();
    }

    private static int reqIntAtLeast(Properties p, String key, int minInclusive) {
        int v = reqInt(p, key);
        if (v < minInclusive) {
            throw new IllegalArgumentException("Property must be >= " + minInclusive + ": " + key + "=" + v);
        }
        return v;
    }

    /** Required double in {@code [0, 1)} (upper bound exclusive for a strict {@code H > T} rule). */
    private static double reqFractionExclusiveUpper(Properties p, String key) {
        double v = reqDouble(p, key);
        if (v < 0 || v >= 1.0) {
            throw new IllegalArgumentException("Property must be in [0, 1): " + key + "=" + v);
        }
        return v;
    }

    private static double reqNonNegativeDouble(Properties p, String key) {
        double v = reqDouble(p, key);
        if (v < 0) {
            throw new IllegalArgumentException("Property must be >= 0: " + key + "=" + v);
        }
        return v;
    }

    private static double reqDouble(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric threshold: " + key + "=" + v);
        }
    }

    private static int reqInt(Properties p, String key) {
        String v = p.getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer threshold: " + key + "=" + v);
        }
    }

    public double lcomMedio() {
        return lcomMedio;
    }

    public double lcomAlto() {
        return lcomAlto;
    }

    public int projectionMedio() {
        return projectionMedio;
    }

    public int projectionAlto() {
        return projectionAlto;
    }

    public double isolatedMedio() {
        return isolatedMedio;
    }

    public double isolatedAlto() {
        return isolatedAlto;
    }

    public int switchMedio() {
        return switchMedio;
    }

    public int switchAlto() {
        return switchAlto;
    }

    public int depthMedio() {
        return depthMedio;
    }

    public int depthAlto() {
        return depthAlto;
    }

    public int indegreeMedio() {
        return indegreeMedio;
    }

    public int indegreeAlto() {
        return indegreeAlto;
    }

    public int implMedio() {
        return implMedio;
    }

    public int implAlto() {
        return implAlto;
    }

    public int instMedio() {
        return instMedio;
    }

    public int instAlto() {
        return instAlto;
    }

    public double outNormMedio() {
        return outNormMedio;
    }

    public double outNormAlto() {
        return outNormAlto;
    }

    public double concreteRatioMedio() {
        return concreteRatioMedio;
    }

    public double concreteRatioAlto() {
        return concreteRatioAlto;
    }

    public double g1OutCentralityMedio() {
        return g1OutCentralityMedio;
    }

    public double g1OutCentralityAlto() {
        return g1OutCentralityAlto;
    }

    public int relaxK() {
        return relaxK;
    }

    public double isolatedMethodsCombinationsK() {
        return isolatedMethodsCombinationsK;
    }

    public double dispatchAstH1HomogeneityThreshold() {
        return dispatchAstH1HomogeneityThreshold;
    }

    public int dispatchAstH2MinChainIfs() {
        return dispatchAstH2MinChainIfs;
    }

    public int dispatchAstH2MinConsecutiveTopIfs() {
        return dispatchAstH2MinConsecutiveTopIfs;
    }

    /** Bundles DISPATCH_AST_HEURISTICS parameters for {@link DispatchAstHeuristicAnalyzer}. */
    public DispatchAstHeuristicAnalyzer.DispatchAstParams dispatchAstParams() {
        return new DispatchAstHeuristicAnalyzer.DispatchAstParams(
                dispatchAstH1HomogeneityThreshold,
                dispatchAstH2MinChainIfs,
                dispatchAstH2MinConsecutiveTopIfs);
    }

    private static final class Builder {
        private double lcomMedio;
        private double lcomAlto;
        private int projectionMedio;
        private int projectionAlto;
        private double isolatedMedio;
        private double isolatedAlto;
        private int switchMedio;
        private int switchAlto;
        private int depthMedio;
        private int depthAlto;
        private int indegreeMedio;
        private int indegreeAlto;
        private int implMedio;
        private int implAlto;
        private int instMedio;
        private int instAlto;
        private double outNormMedio;
        private double outNormAlto;
        private double concreteRatioMedio;
        private double concreteRatioAlto;
        private double g1OutCentralityMedio;
        private double g1OutCentralityAlto;
        private int relaxK;
        private double isolatedMethodsCombinationsK;
        private double dispatchAstH1HomogeneityThreshold;
        private int dispatchAstH2MinChainIfs;
        private int dispatchAstH2MinConsecutiveTopIfs;

        private ThresholdConfiguration build() {
            return new ThresholdConfiguration(this);
        }
    }
}

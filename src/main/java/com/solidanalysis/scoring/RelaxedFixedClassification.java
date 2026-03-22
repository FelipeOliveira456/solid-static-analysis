package com.solidanalysis.scoring;

/**
 * Fixed-threshold classification for small projects using the relaxed/scaled value.
 *
 * <p>For indicators in {@link IndicatorPolicies#isThresholdRelaxedContinuous(IndicatorTemplate)}, the
 * score band is computed from {@code value = rawMetric * f(n)} against the nominal thresholds from
 * {@link ThresholdConfiguration}.
 */
public final class RelaxedFixedClassification {

    private RelaxedFixedClassification() {}

    static double relaxFactor(int classCount, int k) {
        if (classCount <= 0) {
            return 1.0;
        }
        int kk = Math.max(0, k);
        if (kk == 0) {
            return 1.0;
        }
        return (double) classCount / (double) (classCount + kk);
    }

    private static double normalizedF(double f) {
        if (f <= 0 || f > 1.0 || Double.isNaN(f)) {
            return 1.0;
        }
        return f;
    }

    private static double scaled(double raw, double f) {
        return raw * normalizedF(f);
    }

    public static ScoreLevel classifyLcom(double lcom, double f, ThresholdConfiguration t) {
        double value = scaled(lcom, f);
        if (value < t.lcomMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.lcomAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    public static ScoreLevel classifyIsolatedRatio(double ratio, double f, ThresholdConfiguration t) {
        double value = scaled(ratio, f);
        if (value < t.isolatedMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.isolatedAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    public static ScoreLevel classifyOutDegreeNormalized(double ratio, double f, ThresholdConfiguration t) {
        double value = scaled(ratio, f);
        if (value < t.outNormMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.outNormAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Higher concrete ratio = worse (more ALTO). */
    public static ScoreLevel classifyConcreteDependencyRatio(double ratio, double f, ThresholdConfiguration t) {
        double value = scaled(ratio, f);
        if (value < t.concreteRatioMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.concreteRatioAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /**
     * Same band semantics as {@link ClassificationService#classifyImplementsCount(int)}: {@code count
     * < implMedio} BAIXO, {@code count < implAlto} MEDIO, else ALTO.
     */
    public static ScoreLevel classifyImplementsCount(int count, double f, ThresholdConfiguration t) {
        double value = scaled(count, f);
        if (value < t.implMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.implAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /**
     * Same band semantics as configured depth bounds: {@code depth < depthMedio} BAIXO, {@code depth
     * < depthAlto} MEDIO, else ALTO.
     */
    public static ScoreLevel classifyDepth(int depth, double f, ThresholdConfiguration t) {
        double value = scaled(depth, f);
        if (value < t.depthMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.depthAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Uses scaled value ({@code count*f}) against nominal projection thresholds. */
    public static ScoreLevel classifyProjectionClusters(int count, double f, ThresholdConfiguration t) {
        double value = scaled(count, f);
        if (value < t.projectionMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.projectionAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Concrete class in-degree (G2), using {@link ThresholdConfiguration#indegreeMedio()}. */
    public static ScoreLevel classifyConcreteIndegree(int indegree, double f, ThresholdConfiguration t) {
        double value = scaled(indegree, f);
        if (value < t.indegreeMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.indegreeAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Direct instantiations (G1), using {@link ThresholdConfiguration#instMedio()}. */
    public static ScoreLevel classifyInstantiations(int n, double f, ThresholdConfiguration t) {
        double value = scaled(n, f);
        if (value < t.instMedio()) {
            return ScoreLevel.BAIXO;
        }
        if (value < t.instAlto()) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }
}

package com.solidanalysis.scoring;

/**
 * Fixed-threshold classification with effective bounds {@code threshold_base / f(n)} (capped where
 * needed). Used only for the continuous metrics listed in {@link IndicatorPolicies}.
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

    private static double effRatioBound(double base, double f) {
        if (f <= 0 || f > 1.0 || Double.isNaN(f)) {
            return base;
        }
        return Math.min(1.0, base / f);
    }

    public static ScoreLevel classifyLcom(double lcom, double f, ThresholdConfiguration t) {
        double m = effRatioBound(t.lcomMedio(), f);
        double a = effRatioBound(t.lcomAlto(), f);
        if (lcom < m) {
            return ScoreLevel.BAIXO;
        }
        if (lcom < a) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    public static ScoreLevel classifyIsolatedRatio(double ratio, double f, ThresholdConfiguration t) {
        double m = effRatioBound(t.isolatedMedio(), f);
        double a = effRatioBound(t.isolatedAlto(), f);
        if (ratio < m) {
            return ScoreLevel.BAIXO;
        }
        if (ratio < a) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    public static ScoreLevel classifyOutDegreeNormalized(double ratio, double f, ThresholdConfiguration t) {
        double m = effRatioBound(t.outNormMedio(), f);
        double a = effRatioBound(t.outNormAlto(), f);
        if (ratio < m) {
            return ScoreLevel.BAIXO;
        }
        if (ratio < a) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Higher concrete ratio = worse (more ALTO). */
    public static ScoreLevel classifyConcreteDependencyRatio(double ratio, double f, ThresholdConfiguration t) {
        double m = effRatioBound(t.concreteRatioMedio(), f);
        double a = effRatioBound(t.concreteRatioAlto(), f);
        if (ratio < m) {
            return ScoreLevel.BAIXO;
        }
        if (ratio < a) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /**
     * Same band semantics as {@link ClassificationService#classifyImplementsCount(int)}: {@code count
     * < implMedio} BAIXO, {@code count < implAlto} MEDIO, else ALTO.
     */
    public static ScoreLevel classifyImplementsCount(int count, double f, ThresholdConfiguration t) {
        if (f <= 0 || f > 1.0 || Double.isNaN(f)) {
            f = 1.0;
        }
        double b1 = t.implMedio() / f;
        double b2 = t.implAlto() / f;
        if (count < b1) {
            return ScoreLevel.BAIXO;
        }
        if (count < b2) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /**
     * Same band semantics as configured depth bounds: {@code depth < depthMedio} BAIXO, {@code depth
     * < depthAlto} MEDIO, else ALTO.
     */
    public static ScoreLevel classifyDepth(int depth, double f, ThresholdConfiguration t) {
        if (f <= 0 || f > 1.0 || Double.isNaN(f)) {
            f = 1.0;
        }
        double b1 = t.depthMedio() / f;
        double b2 = t.depthAlto() / f;
        if (depth < b1) {
            return ScoreLevel.BAIXO;
        }
        if (depth < b2) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }
}

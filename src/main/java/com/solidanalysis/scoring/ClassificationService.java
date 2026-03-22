package com.solidanalysis.scoring;

import java.util.List;
import java.util.Locale;

/** Maps raw metric values to {@link ScoreLevel} using fixed thresholds or z-scores. */
public final class ClassificationService {

    private final ThresholdConfiguration thresholds;
    private final ScoringStrategy strategy;

    public ClassificationService(ThresholdConfiguration thresholds, ScoringStrategy strategy) {
        this.thresholds = thresholds;
        this.strategy = strategy;
    }

    public ScoringStrategy strategy() {
        return strategy;
    }

    private boolean isFixedThresholdFamily() {
        return strategy == ScoringStrategy.FIXED_THRESHOLD
                || strategy == ScoringStrategy.FIXED_THRESHOLD_RELAXED;
    }

    public ScoreLevel classifyLcom(double lcom) {
        if (isFixedThresholdFamily()) {
            if (lcom < thresholds.lcomMedio()) {
                return ScoreLevel.BAIXO;
            }
            if (lcom < thresholds.lcomAlto()) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally for LCOM");
    }

    public ScoreLevel classifyProjectionClusters(int clusterCount) {
        if (isFixedThresholdFamily()) {
            if (clusterCount < thresholds.projectionMedio()) {
                return ScoreLevel.BAIXO;
            }
            if (clusterCount < thresholds.projectionAlto()) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    public ScoreLevel classifyIsolatedRatio(double ratio) {
        if (isFixedThresholdFamily()) {
            if (ratio < thresholds.isolatedMedio()) {
                return ScoreLevel.BAIXO;
            }
            if (ratio < thresholds.isolatedAlto()) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Switch fan-out: 1-2 BAIXO, 3-4 MEDIO, 5+ ALTO. */
    public ScoreLevel classifySwitchCases(int cases) {
        if (isFixedThresholdFamily()) {
            if (cases <= 2) {
                return ScoreLevel.BAIXO;
            }
            if (cases <= 4) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Depth: 0-1 BAIXO, 2 MEDIO, 3+ ALTO. */
    public ScoreLevel classifyDepth(int depth) {
        if (isFixedThresholdFamily()) {
            if (depth <= 1) {
                return ScoreLevel.BAIXO;
            }
            if (depth == 2) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Concrete in-degree: 0-1 BAIXO, 2-3 MEDIO, 4+ ALTO. */
    public ScoreLevel classifyConcreteIndegree(int indegree) {
        if (isFixedThresholdFamily()) {
            if (indegree <= 1) {
                return ScoreLevel.BAIXO;
            }
            if (indegree <= 3) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Implemented interface count: 0-1 BAIXO, 2-3 MEDIO, 4+ ALTO. */
    public ScoreLevel classifyImplementsCount(int count) {
        if (isFixedThresholdFamily()) {
            if (count <= 1) {
                return ScoreLevel.BAIXO;
            }
            if (count <= 3) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Instantiations: 0 BAIXO, 1-2 MEDIO, 3+ ALTO. */
    public ScoreLevel classifyInstantiations(int n) {
        if (isFixedThresholdFamily()) {
            if (n == 0) {
                return ScoreLevel.BAIXO;
            }
            if (n <= 2) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Normalized out-degree: 0-0.3 BAIXO, 0.3-0.6 MEDIO, 0.6+ ALTO. */
    public ScoreLevel classifyOutDegreeNormalized(double ratio) {
        if (isFixedThresholdFamily()) {
            if (ratio < thresholds.outNormMedio()) {
                return ScoreLevel.BAIXO;
            }
            if (ratio < thresholds.outNormAlto()) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Concrete dependency ratio: 0.0-0.2 BAIXO, 0.2-0.5 MEDIO, 0.5+ ALTO. */
    public ScoreLevel classifyConcreteDependencyRatio(double ratio) {
        if (isFixedThresholdFamily()) {
            if (ratio < thresholds.concreteRatioMedio()) {
                return ScoreLevel.BAIXO;
            }
            if (ratio < thresholds.concreteRatioAlto()) {
                return ScoreLevel.MEDIO;
            }
            return ScoreLevel.ALTO;
        }
        throw new IllegalStateException("Z-score must be applied externally");
    }

    /** Binary / count metrics: ALTO if positive (risk present). */
    public ScoreLevel classifyBooleanRisk(boolean bad) {
        return bad ? ScoreLevel.ALTO : ScoreLevel.BAIXO;
    }

    /**
     * Z-score with spec bands; {@code sigma == 0} → BAIXO for all.
     *
     * @param value raw metric oriented so that larger means worse violation
     */
    public static ScoreLevel classifyZScore(double value, double mean, double sigma) {
        if (sigma == 0.0 || Double.isNaN(sigma)) {
            return ScoreLevel.BAIXO;
        }
        double z = (value - mean) / sigma;
        if (z < 1.0) {
            return ScoreLevel.BAIXO;
        }
        if (z < 2.0) {
            return ScoreLevel.MEDIO;
        }
        return ScoreLevel.ALTO;
    }

    /** Mean of values. */
    public static double mean(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        double s = 0;
        for (double v : values) {
            s += v;
        }
        return s / values.size();
    }

    /** Population standard deviation. */
    public static double sigmaPop(List<Double> values, double mean) {
        if (values.isEmpty()) {
            return 0;
        }
        double s = 0;
        for (double v : values) {
            double d = v - mean;
            s += d * d;
        }
        return Math.sqrt(s / values.size());
    }

    public static String formatMetricDouble(double v) {
        if (v == (long) v) {
            return String.format(Locale.US, "%d", (long) v);
        }
        return String.format(Locale.US, "%.6g", v);
    }
}

package com.solidanalysis.scoring;

/**
 * Maps raw metrics to the {@code "value"} field in scoring JSON for small projects using {@link
 * ScoringStrategy#FIXED_THRESHOLD_RELAXED}.
 *
 * <p>For continuous indicators (see {@link IndicatorPolicies#isThresholdRelaxedContinuous}), bands are
 * computed with bounds widened by {@code 1/f}; comparing {@code raw × f} to the <em>nominal</em>
 * thresholds in {@code analysis.properties} is equivalent for uncapped ratio bounds and the usual
 * count/depth bounds, and keeps reports readable.
 */
public final class RelaxationValueScaling {

    private RelaxationValueScaling() {}

    public static Object scaledValueForJson(
            IndicatorTemplate template, Object rawMetric, ScoringStrategy strategy, double relaxF) {
        if (strategy != ScoringStrategy.FIXED_THRESHOLD_RELAXED) {
            return rawMetric;
        }
        if (!IndicatorPolicies.isThresholdRelaxedContinuous(template)) {
            return rawMetric;
        }
        if (relaxF <= 0 || relaxF > 1.0 + 1e-9 || Double.isNaN(relaxF)) {
            return rawMetric;
        }
        if (rawMetric instanceof Double d) {
            return d * relaxF;
        }
        if (rawMetric instanceof Integer n) {
            return n * relaxF;
        }
        if (rawMetric instanceof Long n) {
            return n * relaxF;
        }
        return rawMetric;
    }
}

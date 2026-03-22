package com.solidanalysis.scoring;

import java.util.Objects;

/** One scored signal contributing to a SOLID principle. */
public final class IndicatorResult {

    private final IndicatorTemplate templateId;
    /** Measurement before JSON-oriented scaling (audit / {@code rawMetric} in export). */
    private final Object rawMetric;
    /**
     * Serialized as {@code "value"} in scoring JSON. For {@link ScoringStrategy#FIXED_THRESHOLD_RELAXED}
     * and templates in {@link IndicatorPolicies#isThresholdRelaxedContinuous}, this is {@code rawMetric
     * × f(n)} (double) so it lines up with <strong>nominal</strong> thresholds in {@code
     * analysis.properties}; otherwise equals {@link #rawMetric()}.
     */
    private final Object value;
    private final String detail;

    public IndicatorResult(IndicatorTemplate templateId, Object value, String detail) {
        this(templateId, value, value, detail);
    }

    public IndicatorResult(
            IndicatorTemplate templateId, Object rawMetric, Object value, String detail) {
        this.templateId = Objects.requireNonNull(templateId);
        this.rawMetric = Objects.requireNonNull(rawMetric);
        this.value = Objects.requireNonNull(value);
        this.detail = Objects.requireNonNull(detail);
    }

    public IndicatorTemplate templateId() {
        return templateId;
    }

    /** Unscaled metric as used internally for classification. */
    public Object rawMetric() {
        return rawMetric;
    }

    /** Value written to JSON as {@code "value"} (scaled when relaxed; see class javadoc). */
    public Object value() {
        return value;
    }

    public String detail() {
        return detail;
    }
}

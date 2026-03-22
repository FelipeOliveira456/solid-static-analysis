package com.solidanalysis.scoring;

import java.util.Objects;

/** One scored signal contributing to a SOLID principle. */
public final class IndicatorResult {

    private final IndicatorTemplate templateId;
    private final Object value;
    private final String detail;

    public IndicatorResult(IndicatorTemplate templateId, Object value, String detail) {
        this.templateId = Objects.requireNonNull(templateId);
        this.value = value;
        this.detail = Objects.requireNonNull(detail);
    }

    public IndicatorTemplate templateId() {
        return templateId;
    }

    /** Raw value used for scoring (JSON-serializable). */
    public Object value() {
        return value;
    }

    public String detail() {
        return detail;
    }
}

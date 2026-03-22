package com.solidanalysis.scoring;

/** How raw metric values are mapped to {@link ScoreLevel}. */
public enum ScoringStrategy {
    FIXED_THRESHOLD,
    /** Fixed thresholds with {@code f(n)=n/(n+k)} scaling on selected continuous metrics. */
    FIXED_THRESHOLD_RELAXED,
    Z_SCORE
}

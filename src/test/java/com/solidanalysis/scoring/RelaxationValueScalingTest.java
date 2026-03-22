package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RelaxationValueScalingTest {

    @Test
    void scalesContinuousMetricsWhenRelaxed() {
        double f = 6.0 / 106.0; // n=6, k=100
        Object v =
                RelaxationValueScaling.scaledValueForJson(
                        IndicatorTemplate.LCOM_VALUE, 0.5, ScoringStrategy.FIXED_THRESHOLD_RELAXED, f);
        assertEquals(0.5 * f, (Double) v, 1e-12);
    }

    @Test
    void leavesRawWhenZScore() {
        Object v =
                RelaxationValueScaling.scaledValueForJson(
                        IndicatorTemplate.LCOM_VALUE, 0.5, ScoringStrategy.Z_SCORE, 0.5);
        assertEquals(0.5, v);
    }

    @Test
    void leavesStructuralIndicatorsUnscaled() {
        Object v =
                RelaxationValueScaling.scaledValueForJson(
                        IndicatorTemplate.SWITCH_CASES,
                        5,
                        ScoringStrategy.FIXED_THRESHOLD_RELAXED,
                        0.2);
        assertEquals(5, v);
    }
}

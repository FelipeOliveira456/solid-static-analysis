package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RelaxedFixedClassificationTest {

    @Test
    void relaxFactorFormula() {
        assertEquals(6.0 / 11.0, RelaxedFixedClassification.relaxFactor(6, 5), 1e-9);
    }

    @Test
    void relaxFactorZeroKReturnsOne() {
        assertEquals(1.0, RelaxedFixedClassification.relaxFactor(6, 0), 0);
    }

    @Test
    void lcomStricterWhenFIsSmall() {
        ThresholdConfiguration t = ThresholdConfiguration.defaults();
        double f = 6.0 / 11.0;
        // With relax, need higher LCOM to reach ALTO than with f=1
        ScoreLevel atF = RelaxedFixedClassification.classifyLcom(0.65, f, t);
        ScoreLevel atOne = RelaxedFixedClassification.classifyLcom(0.65, 1.0, t);
        assertEquals(ScoreLevel.MEDIO, atF);
        assertEquals(ScoreLevel.ALTO, atOne);
    }
}

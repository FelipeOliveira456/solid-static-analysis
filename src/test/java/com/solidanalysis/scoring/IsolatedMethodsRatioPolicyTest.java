package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class IsolatedMethodsRatioPolicyTest {

    private static final double K = 21.0;

    @Test
    void pairCount() {
        assertEquals(0L, IsolatedMethodsRatioPolicy.methodPairCount(0));
        assertEquals(0L, IsolatedMethodsRatioPolicy.methodPairCount(1));
        assertEquals(1L, IsolatedMethodsRatioPolicy.methodPairCount(2));
        assertEquals(10L, IsolatedMethodsRatioPolicy.methodPairCount(5));
        assertEquals(28L, IsolatedMethodsRatioPolicy.methodPairCount(8));
    }

    @Test
    void twoMethodsSmallWeight() {
        double w = IsolatedMethodsRatioPolicy.weightByMethodPairs(2, K); // 1/(1+21)
        assertEquals(1.0 / 22.0, w, 1e-12);
        assertEquals(1.0 / 22.0, IsolatedMethodsRatioPolicy.effectiveRatio(1.0, 2, K), 1e-12);
    }

    @Test
    void eightMethodsPartialWeight() {
        double w = IsolatedMethodsRatioPolicy.weightByMethodPairs(8, K); // 28/(28+21)
        assertEquals(28.0 / 49.0, w, 1e-12);
        assertEquals(0.8 * 28.0 / 49.0, IsolatedMethodsRatioPolicy.effectiveRatio(0.8, 8, K), 1e-12);
    }

    @Test
    void zeroKFullWeightWhenAtLeastTwoMethods() {
        assertEquals(1.0, IsolatedMethodsRatioPolicy.weightByMethodPairs(3, 0.0), 1e-12);
        assertEquals(0.0, IsolatedMethodsRatioPolicy.weightByMethodPairs(1, 0.0), 0);
    }

    @Test
    void negativeKRejected() {
        assertThrows(IllegalArgumentException.class, () -> IsolatedMethodsRatioPolicy.weightByMethodPairs(3, -1));
    }
}

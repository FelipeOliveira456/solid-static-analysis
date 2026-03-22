package com.solidanalysis.scoring;

/**
 * Dampens isolated-method risk for small classes using the number of possible method pairs: few
 * methods imply few opportunities for internal calls, so the same raw isolation ratio is weighted
 * less; many methods make isolation a stronger Single-Responsibility signal.
 *
 * <p>Effective ratio for thresholds / z-score:
 *
 * <pre>
 *   effectiveRatio = rawRatio × C(n,2) / (C(n,2) + k)
 * </pre>
 *
 * where {@code n} is the method count and {@code k ≥ 0} comes from {@code
 * scoring.isolatedMethods.combinations.k} in {@code analysis.properties}. For {@code n &lt; 2},
 * {@code C(n,2) = 0}; with {@code k &gt; 0} the weight is then 0, with {@code k = 0} the weight is
 * defined as 0 to avoid {@code 0/0}.
 */
public final class IsolatedMethodsRatioPolicy {

    private IsolatedMethodsRatioPolicy() {}

    /** Number of unordered pairs of methods: {@code n(n-1)/2}, or 0 if {@code n &lt; 2}. */
    public static long methodPairCount(int methodCount) {
        if (methodCount < 2) {
            return 0L;
        }
        return (long) methodCount * (methodCount - 1) / 2;
    }

    /**
     * Multiplier in {@code [0, 1]} applied to the raw isolated ratio: {@code C(n,2) / (C(n,2) +
     * k)}.
     */
    public static double weightByMethodPairs(int methodCount, double combinationsK) {
        if (combinationsK < 0) {
            throw new IllegalArgumentException("combinationsK must be >= 0, got " + combinationsK);
        }
        double pairs = methodPairCount(methodCount);
        double denom = pairs + combinationsK;
        if (denom <= 0.0) {
            return 0.0;
        }
        return pairs / denom;
    }

    /** Raw isolated ratio × {@link #weightByMethodPairs(int, double)}. */
    public static double effectiveRatio(double rawRatio, int methodCount, double combinationsK) {
        return rawRatio * weightByMethodPairs(methodCount, combinationsK);
    }
}

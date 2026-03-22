package com.solidanalysis.scoring;

/**
 * Classifies indicators for small-project relaxation (threshold scaling) and pairing rules.
 */
public final class IndicatorPolicies {

    private IndicatorPolicies() {}

    /** Structural / architectural signals: never threshold-relaxed; bypass n&lt;5 pairing cap. */
    public static boolean isStructuralIndicator(IndicatorTemplate t) {
        return switch (t) {
            case G3_SCC_CYCLE, G1_CYCLE, EXTENDS_CONCRETE, SWITCH_CASES,
                    INTERFACE_ZERO_INDEGREE_IMPL, INTERFACE_ZERO_INDEGREE_USAGE -> true;
            default -> false;
        };
    }

    /**
     * Continuous / rational metrics whose fixed thresholds are scaled by {@code 1/f(n)} with
     * {@code f(n)=n/(n+k)}.
     */
    public static boolean isThresholdRelaxedContinuous(IndicatorTemplate t) {
        return switch (t) {
            case LCOM_VALUE,
                    ISOLATED_METHODS_RATIO,
                    OUT_DEGREE_NORMALIZED,
                    CONCRETE_DEPENDENCY_RATIO,
                    IMPLEMENTS_COUNT,
                    INHERITANCE_DEPTH -> true;
            default -> false;
        };
    }

    /** Same set as {@link #isThresholdRelaxedContinuous} for the “2× ALTO” rule when n&lt;5. */
    public static boolean isSmallProjectPairingContinuous(IndicatorTemplate t) {
        return isThresholdRelaxedContinuous(t);
    }
}

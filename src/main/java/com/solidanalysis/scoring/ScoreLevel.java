package com.solidanalysis.scoring;

/**
 * Ordered SOLID risk level: {@link #BAIXO} &lt; {@link #MEDIO} &lt; {@link #ALTO}.
 */
public enum ScoreLevel {
    BAIXO,
    MEDIO,
    ALTO;

    /** Returns the worst (highest risk) of the two levels. */
    public static ScoreLevel worst(ScoreLevel a, ScoreLevel b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    public boolean isWorseThan(ScoreLevel other) {
        return this.ordinal() > other.ordinal();
    }
}

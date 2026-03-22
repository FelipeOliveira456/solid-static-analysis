package com.solidanalysis.scoring;

import java.util.Objects;

/**
 * One {@link IndicatorResult} with its own risk level; the principle's aggregate {@link
 * PrincipleScore#score()} is the worst among indicators.
 */
public final class PrincipleIndicator {

    private final IndicatorResult result;
    private final ScoreLevel level;

    public PrincipleIndicator(IndicatorResult result, ScoreLevel level) {
        this.result = Objects.requireNonNull(result);
        this.level = Objects.requireNonNull(level);
    }

    public IndicatorResult result() {
        return result;
    }

    public ScoreLevel level() {
        return level;
    }
}

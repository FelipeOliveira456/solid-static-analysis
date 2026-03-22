package com.solidanalysis.scoring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Aggregated score for one SOLID letter. */
public final class PrincipleScore {

    private final String principle;
    private final ScoreLevel score;
    private final List<PrincipleIndicator> indicators;

    public PrincipleScore(String principle, ScoreLevel score, List<PrincipleIndicator> indicators) {
        this.principle = Objects.requireNonNull(principle);
        this.score = Objects.requireNonNull(score);
        this.indicators = Collections.unmodifiableList(new ArrayList<>(indicators));
    }

    public String principle() {
        return principle;
    }

    public ScoreLevel score() {
        return score;
    }

    public List<PrincipleIndicator> indicators() {
        return indicators;
    }
}

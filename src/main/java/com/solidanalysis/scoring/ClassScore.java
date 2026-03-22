package com.solidanalysis.scoring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Per-class SOLID scoring result (maps to {@code <Class>.json}). */
public final class ClassScore {

    private final String className;
    private final String projectPath;
    private final ScoringStrategy classificationStrategy;
    private final double relaxFactor;
    private final Map<String, PrincipleScore> scores;
    private final ScoreLevel overall;

    public ClassScore(
            String className,
            String projectPath,
            ScoringStrategy classificationStrategy,
            double relaxFactor,
            Map<String, PrincipleScore> scores,
            ScoreLevel overall) {
        this.className = Objects.requireNonNull(className);
        this.projectPath = Objects.requireNonNull(projectPath);
        this.classificationStrategy = Objects.requireNonNull(classificationStrategy);
        this.relaxFactor = relaxFactor;
        this.scores = new LinkedHashMap<>(scores);
        this.overall = Objects.requireNonNull(overall);
    }

    public String className() {
        return className;
    }

    public String projectPath() {
        return projectPath;
    }

    public ScoringStrategy classificationStrategy() {
        return classificationStrategy;
    }

    /** {@code f(n)=n/(n+k)} used for this run (always defined; equals 1.0 when {@code k=0}). */
    public double relaxFactor() {
        return relaxFactor;
    }

    public Map<String, PrincipleScore> scores() {
        return scores;
    }

    public ScoreLevel overall() {
        return overall;
    }
}

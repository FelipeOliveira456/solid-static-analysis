package com.solidanalysis.scoring;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Per-class SOLID scoring result; written under {@code scoring/} mirroring the AST JSON layout. */
public final class ClassScore {

    private final String className;
    /** Package/source folder path under project output, {@code ""} when JSON lives at project root. */
    private final String relativePath;
    private final String projectPath;
    private final ScoringStrategy classificationStrategy;
    private final double relaxFactor;
    private final Map<String, PrincipleScore> scores;
    private final ScoreLevel overall;

    public ClassScore(
            String className,
            String relativePath,
            String projectPath,
            ScoringStrategy classificationStrategy,
            double relaxFactor,
            Map<String, PrincipleScore> scores,
            ScoreLevel overall) {
        this.className = Objects.requireNonNull(className);
        this.relativePath = normalizeRelativePath(relativePath);
        this.projectPath = Objects.requireNonNull(projectPath);
        this.classificationStrategy = Objects.requireNonNull(classificationStrategy);
        this.relaxFactor = relaxFactor;
        this.scores = new LinkedHashMap<>(scores);
        this.overall = Objects.requireNonNull(overall);
    }

    private static String normalizeRelativePath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return "";
        }
        return relativePath.replace('\\', '/');
    }

    public String className() {
        return className;
    }

    /** Mirror of source subfolder; empty string for classes whose AST JSON is at project output root. */
    public String relativePath() {
        return relativePath;
    }

    /** Human-readable label (e.g. {@code domain/Order}). */
    public String displayClassLabel() {
        return relativePath.isEmpty() ? className : relativePath + "/" + className;
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

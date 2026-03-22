package com.solidanalysis.scoring;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Consolidated project scoring ({@code project_summary.json}). */
public final class ProjectSummary {

    private final String projectPath;
    private final ScoringStrategy classificationStrategy;
    private final double relaxFactor;
    private final String mostViolatedPrinciple;
    private final Map<String, Map<String, Integer>> principleDistribution;
    private final List<RankingEntry> ranking;

    public ProjectSummary(
            String projectPath,
            ScoringStrategy classificationStrategy,
            double relaxFactor,
            String mostViolatedPrinciple,
            Map<String, Map<String, Integer>> principleDistribution,
            List<RankingEntry> ranking) {
        this.projectPath = Objects.requireNonNull(projectPath);
        this.classificationStrategy = Objects.requireNonNull(classificationStrategy);
        this.relaxFactor = relaxFactor;
        this.mostViolatedPrinciple = mostViolatedPrinciple;
        this.principleDistribution = Collections.unmodifiableMap(new LinkedHashMap<>(principleDistribution));
        this.ranking = List.copyOf(ranking);
    }

    public String projectPath() {
        return projectPath;
    }

    public ScoringStrategy classificationStrategy() {
        return classificationStrategy;
    }

    public double relaxFactor() {
        return relaxFactor;
    }

    /** Letter {@code S}, {@code O}, {@code L}, {@code I}, {@code D}, or {@code null}. */
    public String mostViolatedPrinciple() {
        return mostViolatedPrinciple;
    }

    public Map<String, Map<String, Integer>> principleDistribution() {
        return principleDistribution;
    }

    public List<RankingEntry> ranking() {
        return ranking;
    }

    /** One row in {@code ranking}. */
    public record RankingEntry(String className, String relativePath, ScoreLevel overall, String worst) {

        public String displayLabel() {
            return relativePath == null || relativePath.isEmpty() ? className : relativePath + "/" + className;
        }
    }
}

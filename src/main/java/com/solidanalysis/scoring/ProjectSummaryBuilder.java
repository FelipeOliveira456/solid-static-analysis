package com.solidanalysis.scoring;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Builds {@link ProjectSummary} from per-class scores. */
public final class ProjectSummaryBuilder {

    private ProjectSummaryBuilder() {}

    public static ProjectSummary build(
            String projectPath, ScoringStrategy strategy, double relaxFactor, List<ClassScore> classes) {
        Map<String, Map<String, Integer>> dist = new LinkedHashMap<>();
        for (String p : List.of("S", "O", "L", "I", "D")) {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("ALTO", 0);
            m.put("MEDIO", 0);
            m.put("BAIXO", 0);
            dist.put(p, m);
        }
        for (ClassScore cs : classes) {
            for (Map.Entry<String, PrincipleScore> e : cs.scores().entrySet()) {
                String letter = e.getKey();
                ScoreLevel sl = e.getValue().score();
                dist.get(letter).merge(sl.name(), 1, Integer::sum);
            }
        }
        String mostViolated = null;
        int bestAlto = -1;
        for (String p : List.of("S", "O", "L", "I", "D")) {
            int alto = dist.get(p).get("ALTO");
            if (alto > bestAlto) {
                bestAlto = alto;
                mostViolated = p;
            } else if (alto == bestAlto && alto > 0) {
                if (mostViolated == null || p.compareTo(mostViolated) < 0) {
                    mostViolated = p;
                }
            }
        }
        if (bestAlto <= 0) {
            mostViolated = null;
        }
        List<ClassScore> sorted = new ArrayList<>(classes);
        sorted.sort(
                Comparator.comparing(ClassScore::overall)
                        .reversed()
                        .thenComparing(ClassScore::className));
        List<ProjectSummary.RankingEntry> ranking = new ArrayList<>();
        for (ClassScore cs : sorted) {
            ranking.add(
                    new ProjectSummary.RankingEntry(
                            cs.className(), cs.overall(), worstPrincipleLetter(cs)));
        }
        return new ProjectSummary(projectPath, strategy, relaxFactor, mostViolated, dist, ranking);
    }

    private static String worstPrincipleLetter(ClassScore cs) {
        ScoreLevel worstLevel = ScoreLevel.BAIXO;
        for (PrincipleScore ps : cs.scores().values()) {
            worstLevel = ScoreLevel.worst(worstLevel, ps.score());
        }
        if (worstLevel == ScoreLevel.BAIXO) {
            return null;
        }
        String pick = null;
        for (String p : List.of("S", "O", "L", "I", "D")) {
            if (cs.scores().get(p).score() == worstLevel) {
                if (pick == null || p.compareTo(pick) < 0) {
                    pick = p;
                }
            }
        }
        return pick;
    }
}

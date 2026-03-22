package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProjectSummaryBuilderTest {

    @Test
    void mostViolatedPrinciplePicksMaxAltoCountTieBreakLexicographic() {
        ClassScore c1 = classWith("A", ScoreLevel.ALTO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO);
        ClassScore c2 = classWith("B", ScoreLevel.BAIXO, ScoreLevel.ALTO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO);
        ClassScore c3 = classWith("C", ScoreLevel.BAIXO, ScoreLevel.ALTO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO);
        ProjectSummary s =
                ProjectSummaryBuilder.build(
                        "/proj", ScoringStrategy.FIXED_THRESHOLD, 1.0, List.of(c1, c2, c3));
        assertEquals("O", s.mostViolatedPrinciple());
        assertEquals(1, s.principleDistribution().get("S").get("ALTO"));
        assertEquals(2, s.principleDistribution().get("O").get("ALTO"));
    }

    @Test
    void mostViolatedNullWhenNoAlto() {
        ClassScore c1 = classWith("A", ScoreLevel.BAIXO, ScoreLevel.MEDIO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO);
        ProjectSummary s =
                ProjectSummaryBuilder.build("/p", ScoringStrategy.FIXED_THRESHOLD, 1.0, List.of(c1));
        assertNull(s.mostViolatedPrinciple());
    }

    @Test
    void rankingSortedByOverallThenName() {
        ClassScore low =
                classWith("Z", ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO, ScoreLevel.BAIXO);
        ClassScore high =
                classWith("A", ScoreLevel.ALTO, ScoreLevel.ALTO, ScoreLevel.ALTO, ScoreLevel.ALTO, ScoreLevel.ALTO);
        ProjectSummary s =
                ProjectSummaryBuilder.build(
                        "/p", ScoringStrategy.FIXED_THRESHOLD, 1.0, List.of(low, high));
        assertEquals("A", s.ranking().get(0).className());
        assertEquals("Z", s.ranking().get(1).className());
    }

    private static ClassScore classWith(
            String name,
            ScoreLevel s,
            ScoreLevel o,
            ScoreLevel l,
            ScoreLevel i,
            ScoreLevel d) {
        Map<String, PrincipleScore> m = new LinkedHashMap<>();
        m.put("S", letter("S", s));
        m.put("O", letter("O", o));
        m.put("L", letter("L", l));
        m.put("I", letter("I", i));
        m.put("D", letter("D", d));
        ScoreLevel overall = ScoreLevel.BAIXO;
        for (PrincipleScore ps : m.values()) {
            overall = ScoreLevel.worst(overall, ps.score());
        }
        return new ClassScore(
                name, "/p", ScoringStrategy.FIXED_THRESHOLD, 1.0, m, overall);
    }

    private static PrincipleScore letter(String p, ScoreLevel level) {
        return new PrincipleScore(p, level, List.of());
    }
}

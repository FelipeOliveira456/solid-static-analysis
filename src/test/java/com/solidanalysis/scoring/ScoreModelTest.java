package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreModelTest {

    @Test
    void scoreLevelWorstPicksHigherOrdinal() {
        assertEquals(ScoreLevel.MEDIO, ScoreLevel.worst(ScoreLevel.BAIXO, ScoreLevel.MEDIO));
        assertEquals(ScoreLevel.ALTO, ScoreLevel.worst(ScoreLevel.MEDIO, ScoreLevel.ALTO));
        assertEquals(ScoreLevel.BAIXO, ScoreLevel.worst(ScoreLevel.BAIXO, ScoreLevel.BAIXO));
    }

    @Test
    void scoreLevelIsWorseThan() {
        assertTrue(ScoreLevel.ALTO.isWorseThan(ScoreLevel.MEDIO));
        assertFalse(ScoreLevel.BAIXO.isWorseThan(ScoreLevel.MEDIO));
    }

    @Test
    void principleScoreCopiesIndicatorsList() {
        List<PrincipleIndicator> ind =
                List.of(
                        new PrincipleIndicator(
                                new IndicatorResult(
                                        IndicatorTemplate.LCOM_VALUE, 0.1, "detail"),
                                ScoreLevel.BAIXO));
        PrincipleScore ps = new PrincipleScore("S", ScoreLevel.BAIXO, ind);
        assertEquals(1, ps.indicators().size());
        assertThrows(
                UnsupportedOperationException.class,
                () ->
                        ps.indicators()
                                .add(
                                        new PrincipleIndicator(
                                                new IndicatorResult(
                                                        IndicatorTemplate.G1_CYCLE, true, "x"),
                                                ScoreLevel.BAIXO)));
    }

    @Test
    void classScoreExposesScoresMap() {
        PrincipleScore s = new PrincipleScore("S", ScoreLevel.BAIXO, List.of());
        java.util.Map<String, PrincipleScore> m = new java.util.LinkedHashMap<>();
        m.put("S", s);
        ClassScore cs =
                new ClassScore(
                        "Foo",
                        "",
                        "/p",
                        ScoringStrategy.FIXED_THRESHOLD_RELAXED,
                        0.5,
                        m,
                        ScoreLevel.BAIXO);
        assertEquals("Foo", cs.className());
        assertEquals(ScoreLevel.BAIXO, cs.overall());
    }

    @Test
    void projectSummaryRankingEntry() {
        ProjectSummary.RankingEntry e =
                new ProjectSummary.RankingEntry("A", "", ScoreLevel.MEDIO, "O");
        assertEquals("A", e.className());
        assertEquals("", e.relativePath());
        assertEquals(ScoreLevel.MEDIO, e.overall());
        assertEquals("O", e.worst());
    }

    @Test
    void scoringStrategyEnumValues() {
        assertEquals(3, ScoringStrategy.values().length);
    }
}

package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class ClassificationServiceTest {

    private final ThresholdConfiguration t = ThresholdConfiguration.defaults();
    private final ClassificationService fixed =
            new ClassificationService(t, ScoringStrategy.FIXED_THRESHOLD);

    @Test
    void classifyLcomBands() {
        assertEquals(ScoreLevel.BAIXO, fixed.classifyLcom(0.2));
        assertEquals(ScoreLevel.MEDIO, fixed.classifyLcom(0.45));
        assertEquals(ScoreLevel.ALTO, fixed.classifyLcom(0.7));
    }

    @Test
    void classifyConcreteIndegreeBands() {
        assertEquals(ScoreLevel.BAIXO, fixed.classifyConcreteIndegree(0));
        assertEquals(ScoreLevel.MEDIO, fixed.classifyConcreteIndegree(2));
        assertEquals(ScoreLevel.ALTO, fixed.classifyConcreteIndegree(5));
    }

    @Test
    void classifyConcreteDependencyRatioBands() {
        assertEquals(ScoreLevel.ALTO, fixed.classifyConcreteDependencyRatio(0.6));
        assertEquals(ScoreLevel.MEDIO, fixed.classifyConcreteDependencyRatio(0.35));
        assertEquals(ScoreLevel.BAIXO, fixed.classifyConcreteDependencyRatio(0.1));
    }

    @Test
    void fixedStrategyRejectsZScorePathForLcom() {
        ClassificationService z = new ClassificationService(t, ScoringStrategy.Z_SCORE);
        assertThrows(IllegalStateException.class, () -> z.classifyLcom(0.5));
    }

    @Test
    void classifyZScoreBands() {
        assertEquals(ScoreLevel.BAIXO, ClassificationService.classifyZScore(5, 5, 0));
        assertEquals(ScoreLevel.BAIXO, ClassificationService.classifyZScore(5, 5, 1));
        assertEquals(ScoreLevel.BAIXO, ClassificationService.classifyZScore(5.9, 5, 1));
        assertEquals(ScoreLevel.MEDIO, ClassificationService.classifyZScore(6.5, 5, 1));
        assertEquals(ScoreLevel.ALTO, ClassificationService.classifyZScore(8, 5, 1));
    }

    @Test
    void meanAndSigmaPop() {
        List<Double> v = List.of(1.0, 2.0, 3.0);
        double m = ClassificationService.mean(v);
        assertEquals(2.0, m, 1e-9);
        double s = ClassificationService.sigmaPop(v, m);
        assertEquals(Math.sqrt(2.0 / 3.0), s, 1e-9);
    }
}

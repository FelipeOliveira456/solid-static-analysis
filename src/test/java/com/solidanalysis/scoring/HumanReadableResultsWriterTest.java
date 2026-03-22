package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HumanReadableResultsWriterTest {

    @Test
    void riskSymbolMapsLevels() {
        assertEquals("[!]", HumanReadableResultsWriter.riskSymbol(ScoreLevel.ALTO));
        assertEquals("[~]", HumanReadableResultsWriter.riskSymbol(ScoreLevel.MEDIO));
        assertEquals("[ok]", HumanReadableResultsWriter.riskSymbol(ScoreLevel.BAIXO));
    }

    @Test
    void principleSectionHeadersUseContractShape() {
        assertEquals(
                "O — Open/Closed: MEDIO",
                HumanReadableResultsWriter.principleSectionHeader("O", ScoreLevel.MEDIO));
        assertEquals(
                "S — Single Responsibility: ALTO",
                HumanReadableResultsWriter.principleSectionHeader("S", ScoreLevel.ALTO));
        assertEquals(
                "D — Dependency Inversion: BAIXO",
                HumanReadableResultsWriter.principleSectionHeader("D", ScoreLevel.BAIXO));
    }

    @Test
    void relaxedStrategyLineIncludesFactor() {
        assertEquals(
                "ESTRATÉGIA: FIXED_THRESHOLD_RELAXED (fator de relaxamento: 0.0566)",
                HumanReadableResultsWriter.formatStrategyLine(
                        ScoringStrategy.FIXED_THRESHOLD_RELAXED, 6.0 / 106.0));
        assertEquals(
                "ESTRATÉGIA: Z_SCORE",
                HumanReadableResultsWriter.formatStrategyLine(ScoringStrategy.Z_SCORE, 1.0));
    }
}

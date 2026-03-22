package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.solidanalysis.SolidAnalysisCli;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HumanReadableResultsProjectSummaryIntegrationTest {

    private static final Pattern RANK_LINE =
            Pattern.compile("^\\s*(\\d+)\\.\\s+(\\S+)\\s+(ALTO|MEDIO|BAIXO)\\s+");

    @Test
    void projectSummaryTxtMatchesJsonRankingAndHeaders(@TempDir Path tmp) throws Exception {
        Path fixture = tmp.resolve("fixture");
        ScoringTestFixtures.copyOutputFixture(fixture);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {"--score", fixture.toAbsolutePath().toString()},
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertTrue(code == SolidAnalysisCli.EXIT_OK, err.toString(StandardCharsets.UTF_8));

        String summaryTxt = HumanReadableResultsTestUtils.readProjectSummaryTxt(fixture);
        assertTrue(summaryTxt.contains("RESUMO DO PROJETO:"));
        assertTrue(summaryTxt.contains("ESTRATÉGIA DE CLASSIFICAÇÃO:"));
        assertTrue(summaryTxt.contains("PRINCÍPIO MAIS VIOLADO:"));

        JsonNode json = HumanReadableResultsTestUtils.loadProjectSummaryJson(fixture);
        JsonNode ranking = json.get("ranking");
        List<String> expectedClasses = new ArrayList<>();
        for (JsonNode row : ranking) {
            expectedClasses.add(row.get("class").asText());
        }

        List<String> parsedClasses = new ArrayList<>();
        boolean inRanking = false;
        for (String line : summaryTxt.split("\r?\n")) {
            if (line.contains("RANKING DE CLASSES (pior para melhor)")) {
                inRanking = true;
                continue;
            }
            if (!inRanking) {
                continue;
            }
            Matcher m = RANK_LINE.matcher(line);
            if (m.find()) {
                parsedClasses.add(m.group(2));
            }
        }
        assertEquals(expectedClasses, parsedClasses, "Ranking class order must match JSON");
    }
}

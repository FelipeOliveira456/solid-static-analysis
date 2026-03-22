package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.solidanalysis.SolidAnalysisCli;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HumanReadableResultsWriterClassReportsIntegrationTest {

    @Test
    void scoreGeneratesClassTxtWithLegendAndContaCorrenteShape(@TempDir Path tmp) throws Exception {
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

        Path scoring = fixture.resolve("scoring");
        try (Stream<Path> stream = Files.walk(scoring)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> !p.getFileName().toString().equals("project_summary.json"))
                    .forEach(
                            p -> {
                                Path relParent = scoring.relativize(p.getParent());
                                String rel =
                                        relParent == null || relParent.toString().isEmpty()
                                                ? ""
                                                : relParent.toString().replace('\\', '/');
                                String base =
                                        p.getFileName()
                                                .toString()
                                                .substring(
                                                        0,
                                                        p.getFileName().toString().length() - 5);
                                Path txtDir = fixture.resolve("results");
                                if (!rel.isEmpty()) {
                                    txtDir = txtDir.resolve(rel);
                                }
                                Path txt = txtDir.resolve(base + ".txt");
                                assertTrue(
                                        Files.exists(txt),
                                        "Missing results for scoring file: " + p.getFileName());
                                try {
                                    String content =
                                            HumanReadableResultsTestUtils.readClassReportTxt(
                                                    fixture, rel, base);
                                    assertTrue(
                                            content.contains("Legenda de símbolos:"),
                                            base + ".txt missing legend");
                                } catch (Exception e) {
                                    throw new AssertionError(e);
                                }
                            });
        }

        String contaTxt =
                HumanReadableResultsTestUtils.readClassReportTxt(fixture, "ContaCorrente");
        assertTrue(contaTxt.contains("CLASSE: ContaCorrente"));
        assertTrue(contaTxt.contains("PROJETO:"));
        assertTrue(contaTxt.contains("SCORE GERAL:"));
        assertTrue(contaTxt.contains("O — Open/Closed:"));
        assertTrue(contaTxt.contains("S — Single Responsibility:"));
        assertTrue(contaTxt.contains("L — Liskov Substitution:"));
        assertTrue(contaTxt.contains("I — Interface Segregation:"));
        assertTrue(contaTxt.contains("D — Dependency Inversion:"));

        JsonNode json =
                HumanReadableResultsTestUtils.loadClassScoreJson(fixture, "ContaCorrente");
        JsonNode scores = json.get("scores");
        for (String letter : java.util.List.of("O", "S", "L", "I", "D")) {
            JsonNode block = scores.get(letter);
            if (block == null) {
                continue;
            }
            JsonNode indicators = block.get("indicators");
            if (indicators == null || !indicators.isArray() || indicators.size() == 0) {
                continue;
            }
            ScoreLevel level = ScoreLevel.valueOf(block.get("score").asText());
            String sym = HumanReadableResultsWriter.riskSymbol(level);
            for (JsonNode ind : indicators) {
                String detail = ind.get("detail").asText();
                assertTrue(
                        contaTxt.contains("  " + sym + " " + detail),
                        "Expected line with symbol "
                                + sym
                                + " and detail fragment for "
                                + letter
                                + ": "
                                + detail);
            }
        }
    }
}

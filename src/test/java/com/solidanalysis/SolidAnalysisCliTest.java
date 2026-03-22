package com.solidanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.scoring.ProjectOutputPathResolver;
import com.solidanalysis.scoring.ScoringTestFixtures;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SolidAnalysisCliTest {

    @Test
    void graphsModeRunsOnAbsoluteDir(@TempDir Path tmp) throws Exception {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {"--graphs", tmp.toAbsolutePath().toString()},
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(SolidAnalysisCli.EXIT_OK, code);
        assertTrue(Files.exists(tmp.resolve("graphs").resolve("g1_dependency.dot")));
    }

    @Test
    void scoreModeRunsOnFixtureOutput(@TempDir Path tmp) throws Exception {
        Path fixture = tmp.resolve("fixture");
        ScoringTestFixtures.copyOutputFixture(fixture);
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {"--score", fixture.toAbsolutePath().toString()},
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(SolidAnalysisCli.EXIT_OK, code, err.toString(StandardCharsets.UTF_8));
        assertTrue(Files.exists(fixture.resolve("scoring").resolve("project_summary.json")));
        assertTrue(Files.exists(fixture.resolve("scoring").resolve("ContaCorrente.json")));
    }

    @Test
    void scoreModeRejectsNonAbsolutePath() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {"--score", "relative/path"},
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(SolidAnalysisCli.EXIT_ERROR, code);
        assertTrue(err.toString(StandardCharsets.UTF_8).contains("absolute"));
    }

    @Test
    void allModeRunsFullPipeline(@TempDir Path workspace) throws Exception {
        Path proj = workspace.resolve("bank");
        ScoringTestFixtures.copyJavaFixtureSources(proj);
        String prev = System.getProperty("user.dir");
        System.setProperty("user.dir", workspace.toAbsolutePath().toString());
        try {
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Path projAbs = proj.toAbsolutePath().normalize();
            int code =
                    SolidAnalysisCli.run(
                            new String[] {"--all", projAbs.toString()},
                            new PrintStream(out, true, StandardCharsets.UTF_8),
                            new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(SolidAnalysisCli.EXIT_OK, code, err.toString(StandardCharsets.UTF_8));
            Path outDir =
                    ProjectOutputPathResolver.resolveProjectOutputDirectory(
                            workspace.resolve("output"), projAbs);
            assertTrue(Files.exists(outDir.resolve("scoring").resolve("project_summary.json")));
        } finally {
            if (prev == null) {
                System.clearProperty("user.dir");
            } else {
                System.setProperty("user.dir", prev);
            }
        }
    }

    @Test
    void allModeSupportsExplicitOutputDirectory(@TempDir Path workspace) throws Exception {
        Path proj = workspace.resolve("bank2");
        ScoringTestFixtures.copyJavaFixtureSources(proj);
        Path explicitOut = workspace.resolve("fixture-output");
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                SolidAnalysisCli.run(
                        new String[] {
                            "--all",
                            proj.toAbsolutePath().toString(),
                            "--output",
                            explicitOut.toAbsolutePath().toString()
                        },
                        new PrintStream(out, true, StandardCharsets.UTF_8),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertEquals(SolidAnalysisCli.EXIT_OK, code, err.toString(StandardCharsets.UTF_8));
        assertTrue(Files.exists(explicitOut.resolve("scoring").resolve("project_summary.json")));
    }
}

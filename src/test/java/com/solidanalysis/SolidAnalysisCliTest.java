package com.solidanalysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}

package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import com.solidanalysis.graphs.io.ProjectOutputLayout;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScannerResilienceTest {

    @Test
    void deveContinuarAposFicheiroInvalidoEContarFalhas(@TempDir Path root) throws Exception {
        Path out = ProjectOutputLayout.astDirectory(root.resolve("output"));
        Files.writeString(root.resolve("Ok.java"), "public class Ok { }\n");
        Files.writeString(root.resolve("Bad.java"), "this is not valid java {{{\n");

        JavaParserFacade facade = new JavaParserFacade(root);
        ProjectScanner scanner =
                new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        ScanRunResult r = scanner.scan(root, out, new PrintStream(log, true, StandardCharsets.UTF_8));

        assertEquals(1, r.getSuccessCount());
        assertEquals(1, r.getFailureCount());
        String combined = log.toString(StandardCharsets.UTF_8);
        assertTrue(combined.contains("Bad.java") || combined.contains("Bad"));
    }
}

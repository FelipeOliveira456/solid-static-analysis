package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProjectScannerIntegrationTest {

    @Test
    void deveGerarDoisJsonParaDoisJavaEmSubpastas(@TempDir Path root) throws Exception {
        Path out = root.resolve("output");
        Files.createDirectories(root.resolve("p1"));
        Files.createDirectories(root.resolve("p2"));
        Files.writeString(
                root.resolve("p1").resolve("A.java"),
                "package p1;\npublic class A { int x; }\n");
        Files.writeString(
                root.resolve("p2").resolve("B.java"),
                "package p2;\npublic class B { void m() { } }\n");

        JavaParserFacade facade = new JavaParserFacade(root);
        ProjectScanner scanner =
                new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
        ByteArrayOutputStream log = new ByteArrayOutputStream();
        ScanRunResult r = scanner.scan(root, out, new PrintStream(log, true, StandardCharsets.UTF_8));

        assertEquals(2, r.getSuccessCount());
        assertEquals(0, r.getFailureCount());
        Path j1 = out.resolve("p1__A.json");
        Path j2 = out.resolve("p2__B.json");
        assertTrue(Files.exists(j1));
        assertTrue(Files.exists(j2));
        assertTrue(Files.size(j1) > 0);
        String c1 = Files.readString(j1, StandardCharsets.UTF_8);
        assertTrue(c1.contains("A"));
    }

    @Test
    void scanSemJavaDeveConcluirComZeroSucessos(@TempDir Path root) throws Exception {
        Path out = root.resolve("output");
        JavaParserFacade facade = new JavaParserFacade(root);
        ProjectScanner scanner =
                new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
        ScanRunResult r = scanner.scan(root, out, new PrintStream(new ByteArrayOutputStream()));

        assertEquals(0, r.getSuccessCount());
        assertEquals(0, r.getFailureCount());
    }
}

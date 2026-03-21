package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScannerCliTest {

    @Test
    void runSemArgsRetornaErro() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code = ScannerCli.run(new String[] {}, new PrintStream(out), new PrintStream(err, true, StandardCharsets.UTF_8));
        assertNotEquals(ScannerCli.EXIT_OK, code);
        assertTrue(
                err.toString(StandardCharsets.UTF_8).toLowerCase().contains("usage")
                        || err.toString(StandardCharsets.UTF_8).length() > 0);
    }

    @Test
    void runComMaisDeUmArgRetornaErro() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                ScannerCli.run(
                        new String[] {"/a", "/b"},
                        new PrintStream(out),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertNotEquals(ScannerCli.EXIT_OK, code);
    }

    @Test
    void runComCaminhoRelativoRetornaErro(@TempDir Path tmp) {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                ScannerCli.run(
                        new String[] {"relative/path"},
                        new PrintStream(out),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertNotEquals(ScannerCli.EXIT_OK, code);
    }

    @Test
    void runComCaminhoInexistenteRetornaErro() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code =
                ScannerCli.run(
                        new String[] {"/nonexistent/path/that/does/not/exist/12345"},
                        new PrintStream(out),
                        new PrintStream(err, true, StandardCharsets.UTF_8));
        assertNotEquals(ScannerCli.EXIT_OK, code);
    }

    @Test
    void runComDiretorioValidoRetornaOk(@TempDir Path tmp) throws Exception {
        Path sub = Files.createDirectories(tmp.resolve("proj"));
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String prev = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", tmp.toString());
            int code =
                    ScannerCli.run(
                            new String[] {sub.toAbsolutePath().toString()},
                            new PrintStream(out, true, StandardCharsets.UTF_8),
                            new PrintStream(err, true, StandardCharsets.UTF_8));
            assertEquals(ScannerCli.EXIT_OK, code);
        } finally {
            if (prev != null) {
                System.setProperty("user.dir", prev);
            } else {
                System.clearProperty("user.dir");
            }
        }
    }
}

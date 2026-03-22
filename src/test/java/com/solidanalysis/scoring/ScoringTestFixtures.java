package com.solidanalysis.scoring;

import com.solidanalysis.fixtures.JavaFixturesPipeline;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Materializes {@code java-fixtures} pipeline output for tests (Etapas 1–4), without relying on
 * committed {@code java-fixtures/output}.
 */
public final class ScoringTestFixtures {

    private ScoringTestFixtures() {}

    /**
     * Runs scan → graphs → analyze → score into {@code targetDir}.
     *
     * @return {@code targetDir}
     */
    public static Path materializeFullFixtureOutput(Path targetDir) throws Exception {
        Path repoRoot = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        JavaFixturesPipeline.runFull(targetDir, repoRoot);
        return targetDir;
    }

    /** Alias for {@link #materializeFullFixtureOutput(Path)}. */
    public static Path copyOutputFixture(Path targetDir) throws Exception {
        return materializeFullFixtureOutput(targetDir);
    }

    public static void copyJavaFixtureSources(Path targetDir) throws IOException {
        ClassLoader cl = ScoringTestFixtures.class.getClassLoader();
        String[] names = {
            "ContaBancaria.java",
            "ContaCorrente.java",
            "ContaPoupanca.java",
            "GerenciadorContas.java",
            "Relatorio.java",
            "Tributavel.java"
        };
        Files.createDirectories(targetDir);
        for (String name : names) {
            try (InputStream in = cl.getResourceAsStream("java-fixtures/" + name)) {
                if (in == null) {
                    throw new IOException("Missing classpath resource: java-fixtures/" + name);
                }
                Files.copy(in, targetDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}

package com.solidanalysis.fixtures;

import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import com.solidanalysis.algorithms.runners.GraphAnalyzeOptions;
import com.solidanalysis.graphs.GraphGenerationRunner;
import com.solidanalysis.scanner.ArtifactJsonWriter;
import com.solidanalysis.scanner.AstExtractor;
import com.solidanalysis.scanner.JavaParserFacade;
import com.solidanalysis.scanner.ProjectScanner;
import com.solidanalysis.scoring.ScoringRunner;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Runs Etapas 1–3 (and optionally 4) from {@code java-fixtures/*.java} into a temp or cache
 * directory — used when {@code src/test/resources/java-fixtures/output/} is not versioned.
 */
public final class JavaFixturesPipeline {

    private JavaFixturesPipeline() {}

    /** Directory containing the six {@code *.java} fixture sources. */
    public static Path javaSourcesRoot() throws Exception {
        var url =
                Objects.requireNonNull(
                        JavaFixturesPipeline.class.getClassLoader()
                                .getResource("java-fixtures/ContaBancaria.java"));
        return Path.of(url.toURI()).getParent();
    }

    public static void runScan(Path projectOut, PrintStream failureLog) throws Exception {
        Path src = javaSourcesRoot();
        Files.createDirectories(projectOut);
        JavaParserFacade facade = new JavaParserFacade(src);
        ProjectScanner scanner =
                new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
        PrintStream log =
                failureLog != null
                        ? failureLog
                        : new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        scanner.scan(src, projectOut, log);
    }

    public static void runGraphs(Path projectOut) throws Exception {
        new GraphGenerationRunner().run(projectOut);
    }

    public static void runAnalyze(Path projectOut, boolean clustering) throws Exception {
        GraphAlgorithmsErrorHandler h = new GraphAlgorithmsErrorHandler();
        boolean ok =
                new GraphAlgorithmsRunner()
                        .run(projectOut, h, new GraphAnalyzeOptions(clustering));
        if (!ok) {
            throw new IOException(
                    "GraphAlgorithmsRunner failed: " + String.join("; ", h.getWarnings()));
        }
    }

    public static void runScore(Path projectOut, Path repoRoot) throws Exception {
        PrintStream err =
                new PrintStream(OutputStream.nullOutputStream(), true, StandardCharsets.UTF_8);
        if (!new ScoringRunner().run(projectOut, repoRoot, err)) {
            throw new IOException("ScoringRunner failed");
        }
    }

    /** Scan → graphs → analyze (Louvain on, aligned with default {@code --all} / {@code --analyze}). */
    public static void runThroughAnalyze(Path projectOut, Path repoRoot) throws Exception {
        runScan(projectOut, null);
        runGraphs(projectOut);
        runAnalyze(projectOut, true);
    }

    /** Full pipeline including scoring. */
    public static void runFull(Path projectOut, Path repoRoot) throws Exception {
        runThroughAnalyze(projectOut, repoRoot);
        runScore(projectOut, repoRoot);
    }
}

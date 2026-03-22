package com.solidanalysis;

import com.solidanalysis.algorithms.runners.GraphAlgorithmsErrorHandler;
import com.solidanalysis.algorithms.runners.GraphAlgorithmsRunner;
import com.solidanalysis.algorithms.runners.GraphAnalyzeOptions;
import com.solidanalysis.graphs.GraphGenerationRunner;
import com.solidanalysis.scanner.ArtifactJsonWriter;
import com.solidanalysis.scanner.AstExtractor;
import com.solidanalysis.scanner.JavaParserFacade;
import com.solidanalysis.scanner.ProjectScanner;
import com.solidanalysis.scanner.ScanRunResult;
import com.solidanalysis.scanner.ScannerCli;
import com.solidanalysis.scoring.ProjectOutputPathResolver;
import com.solidanalysis.scoring.ScoringRunner;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Unified CLI: legacy scan mode (one argument), {@code --graphs}, {@code --analyze},
 * {@code --score}, or {@code --all} (full pipeline into {@code output/&lt;sanitized-root&gt;/}).
 */
public final class SolidAnalysisCli {

    public static final int EXIT_OK = 0;
    public static final int EXIT_ERROR = 1;

    private SolidAnalysisCli() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    /**
     * @return {@link #EXIT_OK} or {@link #EXIT_ERROR}
     */
    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 2 && "--graphs".equals(args[0])) {
            return runGraphs(args[1], err);
        }
        if (args.length >= 2 && "--analyze".equals(args[0])) {
            boolean clustering = Arrays.asList(args).contains("--clustering");
            return runAnalyze(args[1], out, err, clustering);
        }
        if (args.length == 2 && "--score".equals(args[0])) {
            return runScore(args[1], err);
        }
        if (args.length >= 2 && "--all".equals(args[0])) {
            return runAll(args, out, err);
        }
        if (args.length == 1) {
            return ScannerCli.run(args, out, err);
        }
        err.println(
                "Usage: java -jar solid-static-analysis.jar <ABS_ROOT_DIR>\n"
                        + "   or: java -jar solid-static-analysis.jar --graphs <ABS_PROJECT_JSON_DIR>\n"
                        + "   or: java -jar solid-static-analysis.jar --analyze <ABS_PROJECT_OUTPUT_DIR> [--clustering]\n"
                        + "   or: java -jar solid-static-analysis.jar --score <ABS_PROJECT_OUTPUT_DIR>\n"
                        + "   or: java -jar solid-static-analysis.jar --all <ABS_PROJECT_ROOT_DIR> [--output <ABS_PROJECT_OUTPUT_DIR>] [--clustering]\n"
                        + "   or: java -jar solid-static-analysis.jar --all --output <ABS_PROJECT_OUTPUT_DIR> <ABS_PROJECT_ROOT_DIR> [--clustering]");
        return EXIT_ERROR;
    }

    private static Path requireAbsoluteExistingDir(String dirArg, PrintStream err) {
        Path projectDir = Paths.get(dirArg);
        if (!projectDir.isAbsolute()) {
            err.println("Path must be absolute: " + projectDir);
            return null;
        }
        if (!Files.exists(projectDir)) {
            err.println("Path does not exist: " + projectDir);
            return null;
        }
        if (!Files.isDirectory(projectDir)) {
            err.println("Path is not a directory: " + projectDir);
            return null;
        }
        return projectDir;
    }

    private static int runScore(String dirArg, PrintStream err) {
        Path projectDir = requireAbsoluteExistingDir(dirArg, err);
        if (projectDir == null) {
            return EXIT_ERROR;
        }
        Path repoRoot = Paths.get(System.getProperty("user.dir"));
        boolean ok = new ScoringRunner().run(projectDir, repoRoot, err);
        return ok ? EXIT_OK : EXIT_ERROR;
    }

    private record AllOptions(Path root, Path explicitOutputDir, boolean clustering) {}

    private static AllOptions parseAllOptions(String[] args, PrintStream err) {
        Path root = null;
        Path explicitOut = null;
        boolean clustering = false;
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if ("--output".equals(a)) {
                if (i + 1 >= args.length) {
                    err.println("Missing value for --output");
                    return null;
                }
                Path outDir = Paths.get(args[++i]);
                if (!outDir.isAbsolute()) {
                    err.println("Path must be absolute: " + outDir);
                    return null;
                }
                explicitOut = outDir;
            } else if ("--clustering".equals(a)) {
                clustering = true;
            } else if (a.startsWith("--")) {
                err.println("Unknown option for --all: " + a);
                return null;
            } else if (root == null) {
                root = Paths.get(a);
            } else {
                err.println("Too many positional arguments for --all");
                return null;
            }
        }
        if (root == null) {
            err.println("Missing <ABS_PROJECT_ROOT_DIR> for --all");
            return null;
        }
        return new AllOptions(root, explicitOut, clustering);
    }

    private static int runAll(String[] args, PrintStream out, PrintStream err) {
        AllOptions options = parseAllOptions(args, err);
        if (options == null) {
            return EXIT_ERROR;
        }
        Path root = requireAbsoluteExistingDir(options.root().toString(), err);
        if (root == null) {
            return EXIT_ERROR;
        }
        Path outputBase = Paths.get(System.getProperty("user.dir")).resolve("output");
        Path projectOut =
                options.explicitOutputDir() != null
                        ? options.explicitOutputDir().toAbsolutePath().normalize()
                        : ProjectOutputPathResolver.resolveProjectOutputDirectory(outputBase, root);
        try {
            Files.createDirectories(projectOut);
        } catch (IOException e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return EXIT_ERROR;
        }

        try {
            JavaParserFacade facade = new JavaParserFacade(root);
            ProjectScanner scanner =
                    new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
            ScanRunResult result = scanner.scan(root, projectOut, out);
            out.println(
                    "Parsed: "
                            + result.getSuccessCount()
                            + ", Failed: "
                            + result.getFailureCount());
        } catch (IOException e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return EXIT_ERROR;
        }

        try {
            new GraphGenerationRunner().run(projectOut);
        } catch (IOException e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return EXIT_ERROR;
        }

        boolean girvanNewmanClustering = options.clustering();
        GraphAlgorithmsErrorHandler handler = new GraphAlgorithmsErrorHandler();
        boolean analyzeOk =
                new GraphAlgorithmsRunner()
                        .run(projectOut, handler, new GraphAnalyzeOptions(girvanNewmanClustering));
        handler.printWarnings(err);
        if (!analyzeOk) {
            return EXIT_ERROR;
        }
        if (!girvanNewmanClustering) {
            out.println(
                    "Note: Louvain clustering was skipped; add --clustering to --all or use --analyze ... --clustering.");
        }

        Path repoRoot = Paths.get(System.getProperty("user.dir"));
        boolean scoreOk = new ScoringRunner().run(projectOut, repoRoot, err);
        return scoreOk ? EXIT_OK : EXIT_ERROR;
    }

    private static int runAnalyze(
            String dirArg, PrintStream out, PrintStream err, boolean girvanNewmanClustering) {
        Path projectDir = requireAbsoluteExistingDir(dirArg, err);
        if (projectDir == null) {
            return EXIT_ERROR;
        }
        GraphAlgorithmsErrorHandler handler = new GraphAlgorithmsErrorHandler();
        boolean ok =
                new GraphAlgorithmsRunner()
                        .run(projectDir, handler, new GraphAnalyzeOptions(girvanNewmanClustering));
        handler.printWarnings(err);
        if (ok && !girvanNewmanClustering) {
            out.println(
                    "Note: Louvain clustering was skipped; add flag --clustering to compute it.");
        }
        return ok ? EXIT_OK : EXIT_ERROR;
    }

    private static int runGraphs(String dirArg, PrintStream err) {
        Path projectDir = requireAbsoluteExistingDir(dirArg, err);
        if (projectDir == null) {
            return EXIT_ERROR;
        }
        try {
            new GraphGenerationRunner().run(projectDir);
            return EXIT_OK;
        } catch (IOException e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return EXIT_ERROR;
        }
    }
}

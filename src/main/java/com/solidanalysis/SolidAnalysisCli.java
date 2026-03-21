package com.solidanalysis;

import com.solidanalysis.graphs.GraphGenerationRunner;
import com.solidanalysis.scanner.ScannerCli;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unified CLI: legacy scan mode (one argument) or {@code --graphs} to emit DOT files from existing
 * JSON artifacts.
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
        if (args.length == 1) {
            return ScannerCli.run(args, out, err);
        }
        err.println(
                "Usage: java -jar solid-static-analysis.jar <ABS_ROOT_DIR>\n"
                        + "   or: java -jar solid-static-analysis.jar --graphs <ABS_PROJECT_JSON_DIR>");
        return EXIT_ERROR;
    }

    private static int runGraphs(String dirArg, PrintStream err) {
        Path projectDir = Paths.get(dirArg);
        if (!projectDir.isAbsolute()) {
            err.println("Path must be absolute: " + projectDir);
            return EXIT_ERROR;
        }
        if (!Files.exists(projectDir)) {
            err.println("Path does not exist: " + projectDir);
            return EXIT_ERROR;
        }
        if (!Files.isDirectory(projectDir)) {
            err.println("Path is not a directory: " + projectDir);
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

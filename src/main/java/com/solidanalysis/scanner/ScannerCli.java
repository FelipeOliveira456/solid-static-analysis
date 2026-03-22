package com.solidanalysis.scanner;

import com.solidanalysis.graphs.io.ProjectOutputLayout;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Command-line entry point: one argument — absolute path to the project root to scan.
 */
public final class ScannerCli {

    /** Exit code when the process completes a scan (including partial per-file failures). */
    public static final int EXIT_OK = 0;

    /** Exit code for invalid arguments, missing directory, or fatal I/O before scan. */
    public static final int EXIT_ERROR = 1;

    private ScannerCli() {}

    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        System.exit(code);
    }

    /**
     * Runnable from tests without calling {@link System#exit(int)}.
     *
     * @return {@link #EXIT_OK} or {@link #EXIT_ERROR}
     */
    public static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 1) {
            err.println("Usage: java -jar solid-static-analysis.jar <ABS_ROOT_DIR>");
            return EXIT_ERROR;
        }
        Path root = Paths.get(args[0]);
        if (!root.isAbsolute()) {
            err.println("Path must be absolute: " + root);
            return EXIT_ERROR;
        }
        if (!Files.exists(root)) {
            err.println("Path does not exist: " + root);
            return EXIT_ERROR;
        }
        if (!Files.isDirectory(root)) {
            err.println("Path is not a directory: " + root);
            return EXIT_ERROR;
        }

        Path outputDir = ProjectOutputLayout.astDirectory(
                Paths.get(System.getProperty("user.dir")).resolve("output"));
        try {
            JavaParserFacade facade = new JavaParserFacade(root);
            ProjectScanner scanner =
                    new ProjectScanner(facade, new AstExtractor(), new ArtifactJsonWriter());
            ScanRunResult result = scanner.scan(root, outputDir, out);
            out.println(
                    "Parsed: "
                            + result.getSuccessCount()
                            + ", Failed: "
                            + result.getFailureCount());
            return EXIT_OK;
        } catch (IOException e) {
            err.println(e.getMessage() != null ? e.getMessage() : e.toString());
            return EXIT_ERROR;
        }
    }
}

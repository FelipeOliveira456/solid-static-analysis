package com.solidanalysis.scanner;

import com.github.javaparser.ast.CompilationUnit;
import com.solidanalysis.scanner.model.AstArtifact;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Walks a directory tree for {@code .java} files, parses each file, and writes JSON artifacts.
 */
public class ProjectScanner {

    private final JavaParserFacade facade;
    private final AstExtractor extractor;
    private final ArtifactJsonWriter writer;

    public ProjectScanner(JavaParserFacade facade, AstExtractor extractor, ArtifactJsonWriter writer) {
        this.facade = facade;
        this.extractor = extractor;
        this.writer = writer;
    }

    /**
     * Scans {@code scanRoot} recursively and writes one JSON file per successfully parsed source
     * under {@code outputDirectory}.
     *
     * @param failureLog stream where per-file failures are printed (typically {@link System#out})
     */
    public ScanRunResult scan(Path scanRoot, Path outputDirectory, PrintStream failureLog)
            throws IOException {
        Files.createDirectories(outputDirectory);
        ScanRunResult result = new ScanRunResult();
        Path absoluteRoot = scanRoot.toAbsolutePath().normalize();

        try (Stream<Path> stream = Files.walk(absoluteRoot)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(
                            path -> {
                                try {
                                    CompilationUnit cu = facade.parse(path);
                                    AstArtifact artifact = extractor.extract(cu, path);
                                    Path target =
                                            OutputArtifactNamer.resolveOutputPath(
                                                    outputDirectory, absoluteRoot, path);
                                    writer.write(artifact, target);
                                    result.incrementSuccess();
                                } catch (Exception e) {
                                    result.incrementFailure();
                                    String msg =
                                            path.toAbsolutePath()
                                                    + ": "
                                                    + (e.getMessage() != null
                                                            ? e.getMessage()
                                                            : e.getClass().getSimpleName());
                                    result.addFailureMessage(msg);
                                    failureLog.println(msg);
                                }
                            });
        }

        return result;
    }
}

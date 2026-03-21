package com.solidanalysis.scanner;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Configures JavaParser with a {@link CombinedTypeSolver} rooted at the project directory.
 */
public class JavaParserFacade {

    private final JavaParser javaParser;
    private final CombinedTypeSolver combinedTypeSolver;
    private final Path projectRoot;

    /**
     * @param projectRoot absolute directory root passed on the CLI (sources live under this tree)
     */
    public JavaParserFacade(Path projectRoot) {
        this.projectRoot = projectRoot.toAbsolutePath().normalize();
        this.combinedTypeSolver = new CombinedTypeSolver();
        this.combinedTypeSolver.add(new ReflectionTypeSolver());
        this.combinedTypeSolver.add(new JavaParserTypeSolver(this.projectRoot.toFile()));
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setSymbolResolver(new JavaSymbolSolver(combinedTypeSolver));
        this.javaParser = new JavaParser(configuration);
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public CombinedTypeSolver getCombinedTypeSolver() {
        return combinedTypeSolver;
    }

    /**
     * Parses a single {@code .java} file from disk.
     *
     * @return compilation unit when parsing succeeds
     */
    public CompilationUnit parse(Path javaFile) throws IOException {
        ParseResult<CompilationUnit> result = javaParser.parse(javaFile);
        return result.getResult().orElseThrow(() -> new IOException("Parse failed for " + javaFile));
    }
}

package com.solidanalysis.graphs.io;

import java.nio.file.Path;

/**
 * Directory layout under a project pipeline output root (Etapa 1–5).
 *
 * <p>Scanner JSON artifacts live under {@value #AST_DIRECTORY_NAME}/, alongside {@code graphs/},
 * {@code algorithms/}, {@code scoring/}, and {@code results/}.
 */
public final class ProjectOutputLayout {

    /** Name of the subdirectory containing Etapa 1 AST JSON files. */
    public static final String AST_DIRECTORY_NAME = "ast";

    private ProjectOutputLayout() {}

    /** {@code projectOutputDir/ast} */
    public static Path astDirectory(Path projectOutputDir) {
        return projectOutputDir.resolve(AST_DIRECTORY_NAME);
    }
}

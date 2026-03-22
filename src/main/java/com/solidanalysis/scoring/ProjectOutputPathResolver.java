package com.solidanalysis.scoring;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Derives a stable directory name from an absolute path by replacing path separators with
 * underscores (leading underscore), as required for {@code output/&lt;path-sanitized&gt;/}.
 */
public final class ProjectOutputPathResolver {

    private ProjectOutputPathResolver() {}

    /**
     * @param absoluteRoot must be absolute and normalized by the caller if needed
     * @return sanitized segment, e.g. {@code /home/x/p} → {@code _home_x_p}
     */
    public static String sanitizePathSegment(Path absoluteRoot) {
        Objects.requireNonNull(absoluteRoot, "absoluteRoot");
        Path norm = absoluteRoot.toAbsolutePath().normalize();
        String s = norm.toString().replace('\\', '/');
        if (s.isEmpty()) {
            return "_";
        }
        return "_" + s.replace('/', '_');
    }

    /**
     * Resolves {@code outputBase/sanitize(projectRoot)} for pipeline outputs.
     *
     * @param outputBase typically {@code Paths.get(user.dir).resolve("output")}
     * @param projectRoot absolute project root directory
     */
    public static Path resolveProjectOutputDirectory(Path outputBase, Path projectRoot) {
        Objects.requireNonNull(outputBase, "outputBase");
        Objects.requireNonNull(projectRoot, "projectRoot");
        return outputBase.resolve(sanitizePathSegment(projectRoot));
    }
}

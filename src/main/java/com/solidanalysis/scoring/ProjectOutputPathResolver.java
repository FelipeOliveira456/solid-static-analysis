package com.solidanalysis.scoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Resolves where {@code --all} writes project outputs under {@code output/}.
 *
 * <p>When the project root lies inside the working directory (typically the repository root), the
 * output folder mirrors that relative path. If it starts with {@code benchmarks/}, that leading
 * segment is removed so each benchmark writes directly under {@code output/<benchmark-name>/}.
 * Otherwise falls back to {@link #sanitizePathSegment(Path)} (single segment with underscores).
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
     * Same as {@link #resolveProjectOutputDirectory(Path, Path, Path)} using {@code
     * user.dir} as the workspace root.
     */
    public static Path resolveProjectOutputDirectory(Path outputBase, Path projectRoot) {
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        return resolveProjectOutputDirectory(outputBase, projectRoot, cwd);
    }

    /**
     * Resolves the directory for pipeline outputs.
     *
     * <p>If {@code projectRoot} is {@code workspaceRoot} or inside it, returns a relative mirror path
     * under {@code outputBase}. A leading {@code benchmarks} segment is dropped.
     *
     * @param outputBase typically {@code Paths.get(user.dir).resolve("output")}
     * @param projectRoot absolute project root directory
     * @param workspaceRoot absolute working directory root (repository root when using the CLI from
     *     there)
     */
    public static Path resolveProjectOutputDirectory(
            Path outputBase, Path projectRoot, Path workspaceRoot) {
        Objects.requireNonNull(outputBase, "outputBase");
        Objects.requireNonNull(projectRoot, "projectRoot");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        Path baseNorm = outputBase.toAbsolutePath().normalize();
        Path rootNorm = projectRoot.toAbsolutePath().normalize();
        Path cwdNorm = workspaceRoot.toAbsolutePath().normalize();

        if (rootNorm.startsWith(cwdNorm)) {
            Path rel = cwdNorm.relativize(rootNorm);
            Path normalizedRel = stripLeadingBenchmarksSegment(rel);
            if (normalizedRel.getNameCount() == 0 || normalizedRel.toString().isEmpty()) {
                return baseNorm.resolve("_");
            }
            Path candidate = baseNorm.resolve(normalizedRel).normalize();
            if (!candidate.startsWith(baseNorm)) {
                return baseNorm.resolve(sanitizePathSegment(rootNorm));
            }
            return candidate;
        }
        return baseNorm.resolve(sanitizePathSegment(rootNorm));
    }

    static Path stripLeadingBenchmarksSegment(Path rel) {
        if (rel == null || rel.getNameCount() == 0) {
            return Path.of("");
        }
        String first = rel.getName(0).toString();
        if ("benchmarks".equals(first)) {
            return rel.getNameCount() == 1 ? Path.of("") : rel.subpath(1, rel.getNameCount());
        }
        return rel;
    }
}

package com.solidanalysis.scanner;

import java.nio.file.Path;

/**
 * Maps a {@code .java} file under a scan root to a JSON path under the scan output directory
 * (typically {@code <project>/ast/}).
 *
 * <p>Leading Maven/Gradle-style source roots {@code src/main/java} and {@code src/test/java} are
 * <strong>not</strong> reproduced under output (only the path below them is kept), e.g. {@code
 * src/main/java/com/app/Foo.java} → {@code com/app/Foo.json}. Layouts without those prefixes are
 * unchanged (e.g. {@code domain/Order.java} → {@code domain/Order.json}).
 */
public final class OutputArtifactNamer {

    private OutputArtifactNamer() {}

    /**
     * Path of the JSON file relative to the project output directory (slashes as in {@link
     * Path#toString} on the platform).
     *
     * @param scanRoot absolute root of the scanned tree
     * @param javaFile absolute path to a {@code .java} file under {@code scanRoot}
     */
    public static Path relativeJsonPath(Path scanRoot, Path javaFile) {
        Path normalizedRoot = scanRoot.toAbsolutePath().normalize();
        Path normalizedFile = javaFile.toAbsolutePath().normalize();
        Path relative = normalizedRoot.relativize(normalizedFile);
        String fn = relative.getFileName().toString();
        if (!fn.endsWith(".java")) {
            throw new IllegalArgumentException("Expected .java file: " + javaFile);
        }
        String jsonName = fn.substring(0, fn.length() - ".java".length()) + ".json";
        Path parent = relative.getParent();
        Path logicalParent = stripStandardJavaSourceDirectories(parent);
        if (logicalParent.getNameCount() == 0) {
            return Path.of(jsonName);
        }
        return logicalParent.resolve(jsonName);
    }

    /**
     * Removes one or more leading {@code src/(main|test)/java} segments from a directory path
     * relative to the scan root.
     */
    static Path stripStandardJavaSourceDirectories(Path parentRelativeToScanRoot) {
        if (parentRelativeToScanRoot == null) {
            return Path.of("");
        }
        Path p = parentRelativeToScanRoot.normalize();
        if (p.toString().isEmpty()) {
            return Path.of("");
        }
        while (p.getNameCount() >= 3) {
            String s0 = p.getName(0).toString();
            String s1 = p.getName(1).toString();
            String s2 = p.getName(2).toString();
            if ("src".equals(s0)
                    && ("main".equals(s1) || "test".equals(s1))
                    && "java".equalsIgnoreCase(s2)) {
                p = p.getNameCount() == 3 ? Path.of("") : p.subpath(3, p.getNameCount());
                continue;
            }
            break;
        }
        return p.getNameCount() == 0 ? Path.of("") : p;
    }

    /**
     * Unique path string for logging / tests (forward slashes).
     *
     * @see #relativeJsonPath(Path, Path)
     */
    public static String toJsonFileName(Path scanRoot, Path javaFile) {
        return relativeJsonPath(scanRoot, javaFile).toString().replace('\\', '/');
    }

    /**
     * Resolves the full path where the JSON artifact should be written.
     */
    public static Path resolveOutputPath(Path outputDirectory, Path scanRoot, Path javaFile) {
        return outputDirectory.resolve(relativeJsonPath(scanRoot, javaFile)).normalize();
    }
}

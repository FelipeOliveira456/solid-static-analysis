package com.solidanalysis.scanner;

import java.nio.file.Path;

/**
 * Maps a {@code .java} file under a scan root to a unique JSON filename under {@code output/}.
 */
public final class OutputArtifactNamer {

    private OutputArtifactNamer() {}

    /**
     * Builds a unique file name (not a full path) from the path of the source file relative to the
     * scan root (e.g. {@code a/b/Foo.java} → {@code a__b__Foo.json}).
     *
     * @param scanRoot absolute root of the scanned tree
     * @param javaFile absolute path to a {@code .java} file under {@code scanRoot}
     * @return file name ending in {@code .json}
     */
    public static String toJsonFileName(Path scanRoot, Path javaFile) {
        Path normalizedRoot = scanRoot.toAbsolutePath().normalize();
        Path normalizedFile = javaFile.toAbsolutePath().normalize();
        Path relative = normalizedRoot.relativize(normalizedFile);
        String s = relative.toString().replace('\\', '/');
        if (s.endsWith(".java")) {
            s = s.substring(0, s.length() - ".java".length());
        }
        return s.replace("/", "__") + ".json";
    }

    /**
     * Resolves the full path where the JSON artifact should be written.
     */
    public static Path resolveOutputPath(Path outputDirectory, Path scanRoot, Path javaFile) {
        return outputDirectory.resolve(toJsonFileName(scanRoot, javaFile));
    }
}

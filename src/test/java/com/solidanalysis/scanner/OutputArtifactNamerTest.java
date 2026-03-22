package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutputArtifactNamerTest {

    @Test
    void stripsLeadingSrcMainJavaFromOutputPath(@TempDir Path root) throws Exception {
        Path javaFile = root.resolve("src").resolve("main").resolve("java").resolve("org").resolve("demo").resolve("Hello.java");
        Files.createDirectories(javaFile.getParent());
        Files.createFile(javaFile);
        assertEquals("org/demo/Hello.json", OutputArtifactNamer.toJsonFileName(root, javaFile));
    }

    @Test
    void stripsSrcTestJava(@TempDir Path root) throws Exception {
        Path javaFile = root.resolve("src").resolve("test").resolve("java").resolve("T.java");
        Files.createDirectories(javaFile.getParent());
        Files.createFile(javaFile);
        assertEquals("T.json", OutputArtifactNamer.toJsonFileName(root, javaFile));
    }

    @Test
    void deveGerarNomesDistintosParaDoisFooJavaEmSubpastasDiferentes(@TempDir Path root) throws Exception {
        Path a = root.resolve("a").resolve("Foo.java");
        Path b = root.resolve("b").resolve("Foo.java");
        java.nio.file.Files.createDirectories(a.getParent());
        java.nio.file.Files.createDirectories(b.getParent());
        java.nio.file.Files.createFile(a);
        java.nio.file.Files.createFile(b);

        String na = OutputArtifactNamer.toJsonFileName(root, a);
        String nb = OutputArtifactNamer.toJsonFileName(root, b);

        assertNotEquals(na, nb);
    }
}

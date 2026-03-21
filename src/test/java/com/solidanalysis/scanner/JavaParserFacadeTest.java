package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaParserFacadeTest {

    @Test
    void deveParseFicheiroJavaSimples(@TempDir Path root) throws Exception {
        Path f = root.resolve("Hello.java");
        Files.writeString(
                f,
                "package p;\npublic class Hello {\n  public static void main(String[] args) {}\n}\n");

        JavaParserFacade facade = new JavaParserFacade(root);
        CompilationUnit cu = facade.parse(f);

        assertTrue(cu.getTypes().isEmpty() == false);
    }
}

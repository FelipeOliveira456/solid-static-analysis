package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.scanner.model.MethodCallSummary;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TypeResolutionIntegrationTest {

    @Test
    void deveResolverChamadaEntreTiposNoMesmoProjeto(@TempDir Path root) throws Exception {
        Path pkg = Files.createDirectories(root.resolve("p"));
        Files.writeString(
                pkg.resolve("Foo.java"),
                "package p;\npublic class Foo {\n  public void hello() {}\n}\n");
        Files.writeString(
                pkg.resolve("Bar.java"),
                "package p;\npublic class Bar {\n  void m() {\n    Foo f = new Foo();\n    f.hello();\n  }\n}\n");

        JavaParserFacade facade = new JavaParserFacade(root);
        Path bar = pkg.resolve("Bar.java");
        var cu = facade.parse(bar);
        var artifact = new AstExtractor().extract(cu, bar);
        var m =
                artifact.getPrimaryType().getMethods().stream()
                        .filter(x -> x.getName().equals("m"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(
                m.getMethodCalls().stream()
                        .anyMatch(
                                c ->
                                        c.getExpression().contains("new Foo()")
                                                && c.isResolved()
                                                && "p.Foo".equals(c.getDeclaringType())),
                "ObjectCreationExpr (new) deve aparecer em methodCalls com tipo resolvido");
        assertTrue(
                m.getMethodCalls().stream()
                        .anyMatch(
                                c ->
                                        c.getExpression().contains("hello()")
                                                && c.isResolved()),
                "chamada de método após o new");
    }
}

package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ast.CompilationUnit;
import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.MethodSummary;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AstExtractorTest {

    @Test
    void deveExtrairNomeDeClasseSimples(@TempDir Path root) throws Exception {
        Path f = root.resolve("C.java");
        Files.writeString(f, "public class C { }\n");
        JavaParserFacade facade = new JavaParserFacade(root);
        CompilationUnit cu = facade.parse(f);
        AstArtifact a = new AstExtractor().extract(cu, f);
        assertEquals("C", a.getPrimaryType().getName());
        assertEquals("class", a.getPrimaryType().getKind());
    }

    @Test
    void deveRegistarHerancaEInterfacesECampos(@TempDir Path root) throws Exception {
        Path f = root.resolve("Child.java");
        Files.writeString(
                f,
                "public class Child extends Parent implements Runnable, java.io.Serializable {\n"
                        + "  private int x;\n"
                        + "  public void run() {}\n"
                        + "}\n");
        Files.writeString(root.resolve("Parent.java"), "public class Parent { }\n");
        JavaParserFacade facade = new JavaParserFacade(root);
        CompilationUnit cu = facade.parse(f);
        AstArtifact a = new AstExtractor().extract(cu, f);
        assertEquals("Child", a.getPrimaryType().getName());
        assertEquals("Parent", a.getPrimaryType().getSuperclass());
        assertTrue(a.getPrimaryType().getImplementedInterfaces().contains("Runnable"));
        assertTrue(a.getPrimaryType().getImplementedInterfaces().contains("Serializable"));
        assertEquals(1, a.getPrimaryType().getFields().size());
        assertEquals("x", a.getPrimaryType().getFields().get(0).getName());
    }

    @Test
    void deveListarChamadaDeMetodoNoCorpo(@TempDir Path root) throws Exception {
        Path f = root.resolve("T.java");
        Files.writeString(
                f,
                "public class T {\n"
                        + "  void m() {\n"
                        + "    String.valueOf(1);\n"
                        + "  }\n"
                        + "}\n");
        JavaParserFacade facade = new JavaParserFacade(root);
        CompilationUnit cu = facade.parse(f);
        AstArtifact a = new AstExtractor().extract(cu, f);
        MethodSummary m =
                a.getPrimaryType().getMethods().stream()
                        .filter(x -> x.getName().equals("m"))
                        .findFirst()
                        .orElseThrow();
        assertFalse(m.getMethodCalls().isEmpty());
        assertTrue(
                m.getMethodCalls().stream()
                        .anyMatch(c -> c.getExpression().contains("valueOf")));
    }

    @Test
    void deveListarIfEWhileNoCorpo(@TempDir Path root) throws Exception {
        Path f = root.resolve("Flow.java");
        Files.writeString(
                f,
                "public class Flow {\n"
                        + "  void m(int x) {\n"
                        + "    if (x > 0) { }\n"
                        + "    while (x < 10) { x++; }\n"
                        + "  }\n"
                        + "}\n");
        JavaParserFacade facade = new JavaParserFacade(root);
        CompilationUnit cu = facade.parse(f);
        AstArtifact a = new AstExtractor().extract(cu, f);
        MethodSummary m =
                a.getPrimaryType().getMethods().stream()
                        .filter(x -> x.getName().equals("m"))
                        .findFirst()
                        .orElseThrow();
        assertTrue(
                m.getControlFlowStatements().stream()
                        .anyMatch(s -> "if".equals(s.getKind()) && s.getCondition().contains("x > 0")));
        assertTrue(
                m.getControlFlowStatements().stream()
                        .anyMatch(s -> "while".equals(s.getKind()) && s.getCondition().contains("x < 10")));
    }
}

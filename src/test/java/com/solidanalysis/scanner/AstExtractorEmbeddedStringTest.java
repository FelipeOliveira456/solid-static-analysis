package com.solidanalysis.scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.solidanalysis.scanner.model.AstArtifact;
import com.solidanalysis.scanner.model.ControlFlowStatementSummary;
import com.solidanalysis.scanner.model.MethodSummary;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Unit tests with Java source embedded as strings; uses {@link StaticJavaParser#parse(String)} after
 * configuring a symbol resolver (same pattern as production parsing).
 */
class AstExtractorEmbeddedStringTest {

    static {
        ParserConfiguration config =
                new ParserConfiguration()
                        .setSymbolResolver(
                                new JavaSymbolSolver(new CombinedTypeSolver(new ReflectionTypeSolver())));
        StaticJavaParser.setConfiguration(config);
    }

    private static CompilationUnit parseCompilationUnit(String code) {
        return StaticJavaParser.parse(code);
    }

    private static MethodSummary methodNamed(AstArtifact a, String name) {
        return a.getPrimaryType().getMethods().stream()
                .filter(m -> m.getName().equals(name))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void fieldAccessesCapturaLeituraEEscritaEmMetodoSimples() {
        String code =
                "class Fa {\n"
                        + "  private int x;\n"
                        + "  private int y;\n"
                        + "  void m() {\n"
                        + "    int t = x;\n"
                        + "    this.x = 1;\n"
                        + "    y = 2;\n"
                        + "    int u = y;\n"
                        + "  }\n"
                        + "}\n"
                        + "class ContaCorrente {}\n"
                        + "class ContaPoupanca {}\n";
        AstArtifact a = new AstExtractor().extract(parseCompilationUnit(code), Path.of("Fa.java"));
        MethodSummary m = methodNamed(a, "m");
        String owner = "Fa";
        assertTrue(
                m.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "x".equals(fa.getFieldName())
                                                && owner.equals(fa.getOwnerClass())
                                                && "read".equals(fa.getAccessType())));
        assertTrue(
                m.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "x".equals(fa.getFieldName())
                                                && owner.equals(fa.getOwnerClass())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                m.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "y".equals(fa.getFieldName())
                                                && owner.equals(fa.getOwnerClass())
                                                && "write".equals(fa.getAccessType())));
        assertTrue(
                m.getFieldAccesses().stream()
                        .anyMatch(
                                fa ->
                                        "y".equals(fa.getFieldName())
                                                && owner.equals(fa.getOwnerClass())
                                                && "read".equals(fa.getAccessType())));
    }

    @Test
    void ifElseExpoeThenLineEElseLine() {
        String code =
                "class C {\n"
                        + "  void m(boolean a) {\n"
                        + "    if (a) {\n"
                        + "      foo();\n"
                        + "    } else {\n"
                        + "      bar();\n"
                        + "    }\n"
                        + "  }\n"
                        + "  void foo() {}\n"
                        + "  void bar() {}\n"
                        + "}\n";
        AstArtifact art = new AstExtractor().extract(parseCompilationUnit(code), Path.of("C.java"));
        ControlFlowStatementSummary ifStmt =
                methodNamed(art, "m").getControlFlowStatements().stream()
                        .filter(s -> "if".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertEquals(Integer.valueOf(3), ifStmt.getThenLine());
        assertEquals(Integer.valueOf(5), ifStmt.getElseLine());
        assertNotNull(ifStmt.getEndLine());
        assertNull(ifStmt.getChainedElseIf());
    }

    @Test
    void switchListaCasesComLabelELinha() {
        String code =
                "class Sw {\n"
                        + "  void m(int k) {\n"
                        + "    switch (k) {\n"
                        + "      case 1:\n"
                        + "        break;\n"
                        + "      case 2:\n"
                        + "      default:\n"
                        + "        break;\n"
                        + "    }\n"
                        + "  }\n"
                        + "}\n"
                        + "class ContaCorrente {}\n"
                        + "class ContaPoupanca {}\n";
        AstArtifact a = new AstExtractor().extract(parseCompilationUnit(code), Path.of("Sw.java"));
        ControlFlowStatementSummary sw =
                methodNamed(a, "m").getControlFlowStatements().stream()
                        .filter(s -> "switch".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertNotNull(sw.getCases());
        assertTrue(sw.getCases().size() >= 2);
        List<String> labels =
                sw.getCases().stream().map(c -> c.getLabel()).collect(Collectors.toList());
        assertTrue(labels.contains("1"));
        assertTrue(labels.contains("default"));
        assertNotNull(sw.getEndLine());
    }

    @Test
    void whileExpoeEndLine() {
        String code =
                "class W {\n"
                        + "  void m(boolean a) {\n"
                        + "    while (a) {\n"
                        + "      foo();\n"
                        + "    }\n"
                        + "  }\n"
                        + "  void foo() {}\n"
                        + "}\n";
        AstArtifact a = new AstExtractor().extract(parseCompilationUnit(code), Path.of("W.java"));
        ControlFlowStatementSummary w =
                methodNamed(a, "m").getControlFlowStatements().stream()
                        .filter(s -> "while".equals(s.getKind()))
                        .findFirst()
                        .orElseThrow();
        assertEquals(Integer.valueOf(3), w.getThenLine());
        assertNotNull(w.getEndLine());
        assertTrue(w.getEndLine() >= w.getThenLine());
    }

    @Test
    void elseIfEncadeadoNaoGeraDoisIfsSeparados() {
        String code =
                "class E {\n"
                        + "  void m(boolean a, boolean b, boolean c) {\n"
                        + "    if (a) {\n"
                        + "      foo();\n"
                        + "    } else if (b) {\n"
                        + "      bar();\n"
                        + "    } else if (c) {\n"
                        + "      foo();\n"
                        + "    } else {\n"
                        + "      bar();\n"
                        + "    }\n"
                        + "  }\n"
                        + "  void foo() {}\n"
                        + "  void bar() {}\n"
                        + "}\n";
        AstArtifact art = new AstExtractor().extract(parseCompilationUnit(code), Path.of("E.java"));
        List<ControlFlowStatementSummary> flows = methodNamed(art, "m").getControlFlowStatements();
        long ifCount = flows.stream().filter(s -> "if".equals(s.getKind())).count();
        assertEquals(1, ifCount, "else if chain must be a single if node in the list");
        ControlFlowStatementSummary root =
                flows.stream().filter(s -> "if".equals(s.getKind())).findFirst().orElseThrow();
        assertTrue(root.getCondition().contains("a"));
        assertNotNull(root.getChainedElseIf());
        assertTrue(root.getChainedElseIf().getCondition().contains("b"));
        assertNotNull(root.getChainedElseIf().getChainedElseIf());
        assertTrue(root.getChainedElseIf().getChainedElseIf().getCondition().contains("c"));
        assertNull(root.getChainedElseIf().getChainedElseIf().getChainedElseIf());
        assertNotNull(root.getEndLine());
    }
    @Test
    void instantiationsCapturaClassesDoProjetoENaoIncluiJdk() {
        String code =
                "class F {\n"
                        + "  void m() {\n"
                        + "    new ContaCorrente();\n"
                        + "    new ContaPoupanca();\n"
                        + "    new java.util.ArrayList<>();\n"
                        + "  }\n"
                        + "}\n"
                        + "class ContaCorrente {}\n"
                        + "class ContaPoupanca {}\n";
        AstArtifact art = new AstExtractor().extract(parseCompilationUnit(code), Path.of("F.java"));
        MethodSummary m = methodNamed(art, "m");
        assertEquals(2, m.getInstantiations().size());
        assertTrue(
                m.getInstantiations().stream()
                        .anyMatch(i -> "ContaCorrente".equals(i.getType()) && Integer.valueOf(3).equals(i.getLine())));
        assertTrue(
                m.getInstantiations().stream()
                        .anyMatch(i -> "ContaPoupanca".equals(i.getType()) && Integer.valueOf(4).equals(i.getLine())));
    }

    @Test
    void construtorEhExportadoComoMetodoEExtraiEstruturas() {
        String code =
                "class Conta {\n"
                        + "  private Dep dep;\n"
                        + "  Conta() {\n"
                        + "    this.dep = new Dep();\n"
                        + "    if (this.dep != null) { }\n"
                        + "  }\n"
                        + "}\n"
                        + "class Dep {}\n";
        AstArtifact art = new AstExtractor().extract(parseCompilationUnit(code), Path.of("Conta.java"));
        MethodSummary ctor =
                art.getPrimaryType().getMethods().stream()
                        .filter(m -> "Conta".equals(m.getName()) && "<init>".equals(m.getReturnType()))
                        .findFirst()
                        .orElseThrow();
        assertTrue(
                ctor.getFieldAccesses().stream()
                        .anyMatch(fa -> "dep".equals(fa.getFieldName()) && "write".equals(fa.getAccessType())));
        assertTrue(
                ctor.getInstantiations().stream()
                        .anyMatch(i -> "Dep".equals(i.getType()) && Integer.valueOf(4).equals(i.getLine())));
        assertTrue(
                ctor.getControlFlowStatements().stream()
                        .anyMatch(cf -> "if".equals(cf.getKind()) && cf.getThenLine() != null));
    }

}

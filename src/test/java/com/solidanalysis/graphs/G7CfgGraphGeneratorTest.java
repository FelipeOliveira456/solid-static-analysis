package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.ControlFlowStatementSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G7CfgGraphGeneratorTest {

    @Test
    void noCfgFileWhenNoControlFlow(@TempDir Path tmp) throws Exception {
        MethodSummary m =
                new MethodSummary(
                        "plain", "void", List.of(), List.of(), List.of(), List.of(), List.of());
        TypeSummary t =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        ParsedProject p = ParsedProject.fromArtifacts(List.of(new AstArtifact("/t.java", t)));
        Path g7 = tmp.resolve("g7");
        new G7CfgGraphGenerator().writeAll(g7, p);
        try (Stream<Path> w = Files.walk(g7)) {
            long dots =
                    w.filter(Files::isRegularFile)
                            .filter(path -> path.toString().endsWith(".dot"))
                            .count();
            assertEquals(0, dots);
        }
    }

    @Test
    void writesCfgWhenControlFlowPresent(@TempDir Path tmp) throws Exception {
        MethodSummary m =
                new MethodSummary(
                        "go",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(
                                new ControlFlowStatementSummary(
                                        "if", "x", 1, 1, 2, 3, null, null)),
                        List.of(),
                        List.of());
        TypeSummary t =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        ParsedProject p = ParsedProject.fromArtifacts(List.of(new AstArtifact("/t.java", t)));
        Path g7 = tmp.resolve("g7");
        new G7CfgGraphGenerator().writeAll(g7, p);
        Path dotFile = g7.resolve("g7_cfg").resolve("T_go.dot");
        assertTrue(Files.isRegularFile(dotFile), "expected " + dotFile);
    }

    @Test
    void nestedIfInsideLoopBodyAttachesFromParentThenNotEntry(@TempDir Path tmp) throws Exception {
        ControlFlowStatementSummary loop =
                new ControlFlowStatementSummary(
                        "foreach", "int v : valores", 24, 24, null, 29, null, null);
        ControlFlowStatementSummary inner =
                new ControlFlowStatementSummary("if", "v > 0", 25, 25, null, 28, null, null);
        MethodSummary m =
                new MethodSummary(
                        "registrarTransacoes",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(loop, inner),
                        List.of(),
                        List.of());
        TypeSummary t =
                new TypeSummary("class", "T", false, null, List.of(), List.of(), List.of(m));
        ParsedProject p = ParsedProject.fromArtifacts(List.of(new AstArtifact("/t.java", t)));
        Path g7 = tmp.resolve("g7");
        new G7CfgGraphGenerator().writeAll(g7, p);
        String dot =
                Files.readString(
                        g7.resolve("g7_cfg").resolve("T_registrarTransacoes.dot"),
                        StandardCharsets.UTF_8);
        long entryOutEdges = dot.lines().filter(l -> l.trim().startsWith("entry ->")).count();
        assertEquals(1, entryOutEdges, "only the outermost control-flow root should leave entry");
        assertTrue(dot.contains("foreach"), dot);
        assertTrue(dot.contains("if: v > 0"), dot);
        assertTrue(dot.contains("then_"), dot);
        assertFalse(dot.matches("(?s)entry -> s_\\d+;\\s*\\n\\s*entry -> s_\\d+;"));
    }
}

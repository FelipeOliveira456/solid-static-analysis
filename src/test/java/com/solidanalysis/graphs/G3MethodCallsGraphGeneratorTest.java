package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.MethodCallSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G3MethodCallsGraphGeneratorTest {

    @Test
    void skipsJdkCalls(@TempDir Path tmp) throws Exception {
        MethodSummary bm =
                new MethodSummary(
                        "m2", "void", List.of(), List.of(), List.of(), List.of(), List.of());
        TypeSummary bWithM =
                new TypeSummary("class", "B", false, null, List.of(), List.of(), List.of(bm));
        MethodSummary am =
                new MethodSummary(
                        "m1",
                        "void",
                        List.of(),
                        List.of(
                                new MethodCallSummary(
                                        "x",
                                        true,
                                        "java.io.PrintStream",
                                        "java.io.PrintStream.println(java.lang.String)"),
                                new MethodCallSummary("y", true, "B", "B.m2()")),
                        List.of(),
                        List.of(),
                        List.of());
        TypeSummary a =
                new TypeSummary("class", "A", false, null, List.of(), List.of(), List.of(am));
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/a.java", a), new AstArtifact("/b.java", bWithM)));

        Path outDir = tmp.resolve("g3");
        new G3MethodCallsGraphGenerator().writePerClass(outDir, p);

        String aDot = Files.readString(outDir.resolve("A.dot"), StandardCharsets.UTF_8);
        assertTrue(aDot.contains("A.m1"));
        assertTrue(aDot.contains("B.m2"));
        assertFalse(aDot.toLowerCase().contains("printstream"));
    }

    @Test
    void includesInitCall(@TempDir Path tmp) throws Exception {
        MethodSummary bInit =
                new MethodSummary(
                        "B", "<init>", List.of(), List.of(), List.of(), List.of(), List.of());
        TypeSummary b =
                new TypeSummary("class", "B", false, null, List.of(), List.of(), List.of(bInit));
        MethodSummary am =
                new MethodSummary(
                        "build",
                        "void",
                        List.of(),
                        List.of(new MethodCallSummary("new B()", true, "B", "B.B()")),
                        List.of(),
                        List.of(),
                        List.of());
        TypeSummary a =
                new TypeSummary("class", "A", false, null, List.of(), List.of(), List.of(am));
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/a.java", a), new AstArtifact("/b.java", b)));

        Path outDir = tmp.resolve("g3b");
        new G3MethodCallsGraphGenerator().writePerClass(outDir, p);
        String s = Files.readString(outDir.resolve("A.dot"), StandardCharsets.UTF_8);
        assertTrue(s.contains("A.build"));
        assertTrue(s.contains("B.<init>"));
    }
}

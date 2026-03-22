package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.FieldAccessSummary;
import com.solidanalysis.graphs.model.MethodSummary;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G4FieldUsageGraphGeneratorTest {

    @Test
    void perClassFieldUsageContainsOnlyOwnerMethods(@TempDir Path tmp) throws Exception {
        MethodSummary ma =
                new MethodSummary(
                        "run",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("x", "A", "read")),
                        List.of());
        MethodSummary mb =
                new MethodSummary(
                        "pay",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("x", "A", "write")),
                        List.of());
        TypeSummary a =
                new TypeSummary("class", "A", false, null, List.of(), List.of(), List.of(ma));
        TypeSummary b =
                new TypeSummary("class", "B", false, null, List.of(), List.of(), List.of(mb));
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/a.java", a), new AstArtifact("/b.java", b)));

        Path outDir = tmp.resolve("g4_field_usage");
        new G4FieldUsageGraphGenerator().writeFieldUsagePerClass(outDir, p);

        Path layer = outDir.resolve("g4_field_usage");
        String aDot = Files.readString(layer.resolve("A.dot"), StandardCharsets.UTF_8);
        String bDot = Files.readString(layer.resolve("B.dot"), StandardCharsets.UTF_8);
        assertTrue(aDot.contains("A.run"));
        assertFalse(aDot.contains("B.pay"));
        assertTrue(bDot.contains("B.pay"));
        assertFalse(bDot.contains("A.run"));
    }

    @Test
    void projectionDoesNotConnectDifferentClasses(@TempDir Path tmp) throws Exception {
        MethodSummary mBase =
                new MethodSummary(
                        "init",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("saldo", "ContaBancaria", "read")),
                        List.of());
        MethodSummary mChild =
                new MethodSummary(
                        "cobrarTaxa",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("saldo", "ContaBancaria", "write")),
                        List.of());

        TypeSummary base =
                new TypeSummary(
                        "class", "ContaBancaria", false, null, List.of(), List.of(), List.of(mBase));
        TypeSummary child =
                new TypeSummary(
                        "class",
                        "ContaCorrente",
                        false,
                        "ContaBancaria",
                        List.of(),
                        List.of(),
                        List.of(mChild));

        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(
                                new AstArtifact("/base.java", base),
                                new AstArtifact("/child.java", child)));

        Path projDir = tmp.resolve("g4_method_projection");
        new G4FieldUsageGraphGenerator().writeMethodProjectionPerClass(projDir, p);

        Path layer = projDir.resolve("g4_method_projection");
        String baseDot = Files.readString(layer.resolve("ContaBancaria.dot"), StandardCharsets.UTF_8);
        String childDot = Files.readString(layer.resolve("ContaCorrente.dot"), StandardCharsets.UTF_8);

        // one method per class => no projection edges; method–method projection is undirected
        assertTrue(baseDot.trim().startsWith("graph "));
        assertTrue(childDot.trim().startsWith("graph "));
        assertFalse(baseDot.contains("->"));
        assertFalse(childDot.contains("->"));
    }

    @Test
    void methodProjectionUsesUndirectedEdges(@TempDir Path tmp) throws Exception {
        MethodSummary m1 =
                new MethodSummary(
                        "a",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("x", "A", "read")),
                        List.of());
        MethodSummary m2 =
                new MethodSummary(
                        "b",
                        "void",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(new FieldAccessSummary("x", "A", "read")),
                        List.of());
        TypeSummary t =
                new TypeSummary("class", "A", false, null, List.of(), List.of(), List.of(m1, m2));
        ParsedProject p =
                ParsedProject.fromArtifacts(List.of(new AstArtifact("/a.java", t)));
        Path out = tmp.resolve("g4_method_projection");
        new G4FieldUsageGraphGenerator().writeMethodProjectionPerClass(out, p);
        String dot =
                Files.readString(out.resolve("g4_method_projection").resolve("A.dot"), StandardCharsets.UTF_8);
        assertTrue(dot.trim().startsWith("graph "));
        assertTrue(dot.contains(" -- "));
        assertFalse(dot.contains("->"));
    }
}

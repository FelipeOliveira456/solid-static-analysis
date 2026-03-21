package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.FieldSummary;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G1DependencyGraphGeneratorTest {

    @Test
    void projectFieldDependencyHasFieldLabel(@TempDir Path tmp) throws Exception {
        TypeSummary b =
                new TypeSummary(
                        "class", "B", false, null, List.of(), List.of(), List.of());
        TypeSummary a =
                new TypeSummary(
                        "class",
                        "A",
                        false,
                        null,
                        List.of(),
                        List.of(new FieldSummary("s", "String"), new FieldSummary("b", "B")),
                        List.of());
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/a.java", a), new AstArtifact("/b.java", b)));
        Path out = tmp.resolve("g1.dot");
        new G1DependencyGraphGenerator().write(out, p);
        String dot = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(dot.contains("field"));
        assertTrue(dot.contains("A") && dot.contains("B"));
    }
}

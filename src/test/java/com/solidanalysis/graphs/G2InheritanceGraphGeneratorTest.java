package com.solidanalysis.graphs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.ParsedProject;
import com.solidanalysis.graphs.model.TypeSummary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class G2InheritanceGraphGeneratorTest {

    @Test
    void extendsAndImplementsLabels(@TempDir Path tmp) throws Exception {
        TypeSummary iface =
                new TypeSummary(
                        "interface", "I", false, null, List.of(), List.of(), List.of());
        TypeSummary parent =
                new TypeSummary("class", "P", false, null, List.of(), List.of(), List.of());
        TypeSummary child =
                new TypeSummary(
                        "class", "C", false, "P", List.of("I"), List.of(), List.of());
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(
                                new AstArtifact("/c.java", child),
                                new AstArtifact("/p.java", parent),
                                new AstArtifact("/i.java", iface)));
        Path out = tmp.resolve("g2.dot");
        new G2InheritanceGraphGenerator().write(out, p);
        String s = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(s.contains("extends"));
        assertTrue(s.contains("implements"));
    }
}

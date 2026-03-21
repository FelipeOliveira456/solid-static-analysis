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

class G5InterfaceImplGraphGeneratorTest {

    @Test
    void shapesDiffer(@TempDir Path tmp) throws Exception {
        TypeSummary iface =
                new TypeSummary(
                        "interface", "Ix", false, null, List.of(), List.of(), List.of());
        TypeSummary clazz =
                new TypeSummary(
                        "class", "Cx", false, null, List.of("Ix"), List.of(), List.of());
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/c.java", clazz), new AstArtifact("/i.java", iface)));
        Path out = tmp.resolve("g5.dot");
        new G5InterfaceImplGraphGenerator().write(out, p);
        String s = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(s.contains("shape=ellipse"));
        assertTrue(s.contains("shape=box"));
    }

    @Test
    void onlyIfaceTypes(@TempDir Path tmp) throws Exception {
        TypeSummary i1 =
                new TypeSummary(
                        "interface", "I1", false, null, List.of(), List.of(), List.of());
        TypeSummary i2 =
                new TypeSummary(
                        "interface", "I2", false, null, List.of(), List.of(), List.of());
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/1.java", i1), new AstArtifact("/2.java", i2)));
        Path out = tmp.resolve("g5b.dot");
        new G5InterfaceImplGraphGenerator().write(out, p);
        String s = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(s.contains("digraph"));
    }
}

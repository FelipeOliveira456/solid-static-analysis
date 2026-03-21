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

class G6InterfaceUsageGraphGeneratorTest {

    @Test
    void edgeWhenFieldUsesIface(@TempDir Path tmp) throws Exception {
        TypeSummary iface =
                new TypeSummary(
                        "interface", "Ix", false, null, List.of(), List.of(), List.of());
        TypeSummary clazz =
                new TypeSummary(
                        "class",
                        "Cx",
                        false,
                        null,
                        List.of(),
                        List.of(new FieldSummary("i", "Ix")),
                        List.of());
        ParsedProject p =
                ParsedProject.fromArtifacts(
                        List.of(new AstArtifact("/c.java", clazz), new AstArtifact("/i.java", iface)));
        Path out = tmp.resolve("g6.dot");
        new G6InterfaceUsageGraphGenerator().write(out, p);
        String s = Files.readString(out, StandardCharsets.UTF_8);
        assertTrue(s.contains("Cx"));
        assertTrue(s.contains("Ix"));
    }
}

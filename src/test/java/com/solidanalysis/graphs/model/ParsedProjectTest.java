package com.solidanalysis.graphs.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ParsedProjectTest {

    @Test
    void indicesContemNomesDeTipos() {
        TypeSummary t =
                new TypeSummary(
                        "class",
                        "Demo",
                        false,
                        null,
                        List.of(),
                        List.of(),
                        List.of());
        AstArtifact a = new AstArtifact("/x.java", t);
        ParsedProject p = ParsedProject.fromArtifacts(List.of(a));
        assertTrue(p.projectTypeNames().contains("Demo"));
        assertEquals(t, p.typeOrNull("Demo"));
    }
}

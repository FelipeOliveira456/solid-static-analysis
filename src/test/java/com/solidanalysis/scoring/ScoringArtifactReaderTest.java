package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.solidanalysis.fixtures.JavaFixturesPipeline;
import com.solidanalysis.graphs.model.AstArtifact;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScoringArtifactReaderTest {

    @Test
    void parseG2ExtendsReadsLabeledEdges(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("g2.dot");
        Files.writeString(
                f,
                "A -> B [label=\"extends\"];\nC -> D [label=\"implements\"];\n",
                StandardCharsets.UTF_8);
        ScoringArtifactReader r = new ScoringArtifactReader();
        Map<String, String> m = r.parseG2Extends(f);
        assertEquals("B", m.get("A"));
        assertEquals(1, m.size());
    }

    @Test
    void parseG1OutgoingTargetsCollectsDistinct(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("g1.dot");
        Files.writeString(
                f,
                "X -> Y;\nX -> Z [label=\"call\"];\nX -> Y;\n",
                StandardCharsets.UTF_8);
        ScoringArtifactReader r = new ScoringArtifactReader();
        Set<String> t = r.parseG1OutgoingTargets(f, "X");
        assertEquals(Set.of("Y", "Z"), t);
    }

    @Test
    void parseG1InstantiationOutCounts(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("g1.dot");
        Files.writeString(
                f,
                "A -> B [label=\"instantiation\"];\nA -> C [label=\"instantiation\"];\n",
                StandardCharsets.UTF_8);
        Map<String, Integer> c = new ScoringArtifactReader().parseG1InstantiationOutCounts(f);
        assertEquals(2, c.get("A"));
    }

    @Test
    void parseG5EllipseInterfaces(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("g5.dot");
        Files.writeString(
                f,
                "Runnable [shape=ellipse, label=\"Runnable\"];\n",
                StandardCharsets.UTF_8);
        Set<String> ids = new ScoringArtifactReader().parseG5EllipseInterfaces(f);
        assertTrue(ids.contains("Runnable"));
    }

    @Test
    void parseG6FiltersToKnownInterfaces(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("g6.dot");
        Files.writeString(
                f,
                "Client -> Runnable;\nClient -> Other;\n",
                StandardCharsets.UTF_8);
        Set<String> ifaces = Set.of("Runnable");
        Map<String, Set<String>> u =
                new ScoringArtifactReader().parseG6ClientInterfaceUsage(f, ifaces);
        assertEquals(Set.of("Runnable"), u.get("Client"));
    }

    @Test
    void parseG7MaxSwitchFanout(@TempDir Path tmp) throws Exception {
        Path f = tmp.resolve("cfg.dot");
        Files.writeString(
                f,
                """
                n1 [label="switch (x)"];
                n1 -> b1;
                n1 -> b2;
                n1 -> b3;
                other [label="foo"];
                other -> x1;
                """,
                StandardCharsets.UTF_8);
        int max = new ScoringArtifactReader().parseG7MaxSwitchFanout(f);
        assertEquals(3, max);
    }

    @Test
    void g7BaseNameHelpers() {
        assertEquals("ContaCorrente", ScoringArtifactReader.classNameFromG7Base("ContaCorrente_foo"));
        assertEquals("foo", ScoringArtifactReader.methodNameFromG7Base("ContaCorrente_foo"));
    }

    @Test
    void loadAstArtifactsFromFixtureOutput(@TempDir Path tmp) throws Exception {
        JavaFixturesPipeline.runScan(tmp, null);
        List<AstArtifact> arts = new ScoringArtifactReader().loadAstArtifacts(tmp);
        assertTrue(arts.size() >= 6);
        List<String> names = ScoringArtifactReader.classNamesSorted(arts);
        assertTrue(names.contains("ContaCorrente"));
    }
}

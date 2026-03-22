package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ThresholdConfigurationTest {

    @Test
    void defaultsMatchSpec() {
        ThresholdConfiguration c = ThresholdConfiguration.defaults();
        assertEquals(0.3, c.lcomMedio());
        assertEquals(0.6, c.lcomAlto());
        assertEquals(2, c.projectionMedio());
        assertEquals(3, c.projectionAlto());
        assertEquals(2, c.indegreeMedio());
        assertEquals(4, c.indegreeAlto());
    }

    @Test
    void loadOverridesFromAnalysisProperties(@TempDir Path tmp) throws Exception {
        Files.writeString(
                tmp.resolve("analysis.properties"),
                "threshold.lcom.medio=0.5\nthreshold.lcom.alto=0.8\n");
        ThresholdConfiguration c = ThresholdConfiguration.load(tmp);
        assertEquals(0.5, c.lcomMedio());
        assertEquals(0.8, c.lcomAlto());
    }

    @Test
    void loadMissingFileUsesDefaults(@TempDir Path tmp) throws Exception {
        ThresholdConfiguration c = ThresholdConfiguration.load(tmp);
        assertEquals(0.3, c.lcomMedio());
    }

    @Test
    void loadRejectsInvalidNumber(@TempDir Path tmp) throws Exception {
        Files.writeString(tmp.resolve("analysis.properties"), "threshold.lcom.medio=not-a-number\n");
        assertThrows(IllegalArgumentException.class, () -> ThresholdConfiguration.load(tmp));
    }

    @Test
    void loadRelaxKFromProperties(@TempDir Path tmp) throws Exception {
        Files.writeString(tmp.resolve("analysis.properties"), "scoring.relax.k=10\n");
        ThresholdConfiguration c = ThresholdConfiguration.load(tmp);
        assertEquals(10, c.relaxK());
    }
}

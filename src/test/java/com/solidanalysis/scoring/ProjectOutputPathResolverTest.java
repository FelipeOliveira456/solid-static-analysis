package com.solidanalysis.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ProjectOutputPathResolverTest {

    @Test
    void sanitizePathSegmentReplacesSeparatorsWithUnderscores() {
        Path root = Paths.get("/tmp/x");
        String seg = ProjectOutputPathResolver.sanitizePathSegment(root);
        // Leading "/" becomes "_" in the body, then resolver adds another leading "_"
        assertTrue(seg.startsWith("_"));
        assertTrue(seg.endsWith("tmp_x"));
    }

    @Test
    void resolveProjectOutputDirectoryUnderBase() {
        Path base = Paths.get("/tmp/out");
        Path project = Paths.get("/abs/proj").toAbsolutePath();
        Path resolved = ProjectOutputPathResolver.resolveProjectOutputDirectory(base, project);
        assertEquals(
                base.resolve(ProjectOutputPathResolver.sanitizePathSegment(project)), resolved);
    }
}

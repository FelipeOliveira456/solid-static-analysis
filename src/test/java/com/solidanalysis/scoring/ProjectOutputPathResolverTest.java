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
        Path workspace = Paths.get("/tmp/ws").toAbsolutePath();
        Path resolved =
                ProjectOutputPathResolver.resolveProjectOutputDirectory(base, project, workspace);
        assertEquals(
                base.resolve(ProjectOutputPathResolver.sanitizePathSegment(project)), resolved);
    }

    @Test
    void resolveProjectOutputDirectoryDropsLeadingBenchmarksSegmentInsideWorkspace() {
        Path workspace = Paths.get("/tmp/ws").toAbsolutePath();
        Path base = workspace.resolve("output");
        Path project = workspace.resolve("benchmarks").resolve("good-project");
        Path resolved =
                ProjectOutputPathResolver.resolveProjectOutputDirectory(base, project, workspace);
        assertEquals(base.resolve("good-project"), resolved);
    }

    @Test
    void resolveProjectOutputDirectoryKeepsNonBenchmarksRelativePathInsideWorkspace() {
        Path workspace = Paths.get("/tmp/ws").toAbsolutePath();
        Path base = workspace.resolve("output");
        Path project = workspace.resolve("examples").resolve("bank");
        Path resolved =
                ProjectOutputPathResolver.resolveProjectOutputDirectory(base, project, workspace);
        assertEquals(base.resolve("examples").resolve("bank"), resolved);
    }

}

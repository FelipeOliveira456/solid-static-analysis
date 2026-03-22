package com.solidanalysis.scoring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Test helpers to read Etapa 4 JSON and Etapa 5 human-readable outputs side by side. */
public final class HumanReadableResultsTestUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private HumanReadableResultsTestUtils() {}

    /** {@code scoring/[relativePath/]<className>.json} */
    public static JsonNode loadClassScoreJson(Path projectOutputDir, String className)
            throws IOException {
        return loadClassScoreJson(projectOutputDir, "", className);
    }

    /** {@code scoring/[relativePath/]<className>.json} */
    public static JsonNode loadClassScoreJson(Path projectOutputDir, String relativePath, String className)
            throws IOException {
        Path dir = projectOutputDir.resolve("scoring");
        if (relativePath != null && !relativePath.isEmpty()) {
            dir = dir.resolve(relativePath);
        }
        Path p = dir.resolve(className + ".json");
        return MAPPER.readTree(p.toFile());
    }

    /** {@code scoring/project_summary.json} */
    public static JsonNode loadProjectSummaryJson(Path projectOutputDir) throws IOException {
        Path p = projectOutputDir.resolve("scoring").resolve("project_summary.json");
        return MAPPER.readTree(p.toFile());
    }

    /** {@code results/[relativePath/]<className>.txt} */
    public static String readClassReportTxt(Path projectOutputDir, String className)
            throws IOException {
        return readClassReportTxt(projectOutputDir, "", className);
    }

    /** {@code results/[relativePath/]<className>.txt} */
    public static String readClassReportTxt(Path projectOutputDir, String relativePath, String className)
            throws IOException {
        Path dir = projectOutputDir.resolve("results");
        if (relativePath != null && !relativePath.isEmpty()) {
            dir = dir.resolve(relativePath);
        }
        Path p = dir.resolve(className + ".txt");
        return Files.readString(p, StandardCharsets.UTF_8);
    }

    /** {@code results/project_summary.txt} */
    public static String readProjectSummaryTxt(Path projectOutputDir) throws IOException {
        Path p = projectOutputDir.resolve("results").resolve("project_summary.txt");
        return Files.readString(p, StandardCharsets.UTF_8);
    }
}

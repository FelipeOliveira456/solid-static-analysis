package com.solidanalysis.scoring;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Writes {@code scoring/<Class>.json} and {@code project_summary.json}. */
public final class ScoringReportWriter {

    private final ObjectMapper mapper;

    public ScoringReportWriter() {
        this.mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        mapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public void writeClassScore(Path scoringDir, ClassScore cs) throws IOException {
        Path dir = scoringDir;
        if (!cs.relativePath().isEmpty()) {
            dir = scoringDir.resolve(cs.relativePath());
        }
        Files.createDirectories(dir);
        Path out = dir.resolve(cs.className() + ".json");
        mapper.writeValue(out.toFile(), toJsonMap(cs));
    }

    public void writeProjectSummary(Path scoringDir, ProjectSummary summary) throws IOException {
        Files.createDirectories(scoringDir);
        Path out = scoringDir.resolve("project_summary.json");
        mapper.writeValue(out.toFile(), toJsonMap(summary));
    }

    private static Map<String, Object> toJsonMap(ClassScore cs) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("class", cs.className());
        root.put("relativePath", cs.relativePath());
        root.put("classificationStrategy", cs.classificationStrategy().name());
        root.put("overall", cs.overall().name());
        root.put("projectPath", cs.projectPath());
        root.put("relaxFactor", cs.relaxFactor());
        Map<String, Object> scores = new LinkedHashMap<>();
        for (Map.Entry<String, PrincipleScore> e : cs.scores().entrySet()) {
            scores.put(e.getKey(), principleToMap(e.getValue()));
        }
        root.put("scores", scores);
        return root;
    }

    private static Map<String, Object> principleToMap(PrincipleScore p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("score", p.score().name());
        List<Map<String, Object>> inds = new ArrayList<>();
        for (PrincipleIndicator pi : p.indicators()) {
            IndicatorResult ir = pi.result();
            Map<String, Object> im = new LinkedHashMap<>();
            im.put("templateId", ir.templateId().jsonName());
            im.put("value", ir.value());
            if (!Objects.equals(ir.rawMetric(), ir.value())) {
                im.put("rawMetric", ir.rawMetric());
            }
            im.put("detail", ir.detail());
            im.put("level", pi.level().name());
            inds.add(im);
        }
        m.put("indicators", inds);
        return m;
    }

    private static Map<String, Object> toJsonMap(ProjectSummary s) {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("classificationStrategy", s.classificationStrategy().name());
        root.put("mostViolatedPrinciple", s.mostViolatedPrinciple());
        root.put("projectPath", s.projectPath());
        root.put("relaxFactor", s.relaxFactor());
        root.put("principleDistribution", s.principleDistribution());
        List<Map<String, Object>> rank = new ArrayList<>();
        for (ProjectSummary.RankingEntry e : s.ranking()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("class", e.className());
            row.put("relativePath", e.relativePath());
            row.put("overall", e.overall().name());
            row.put("worst", e.worst());
            rank.add(row);
        }
        root.put("ranking", rank);
        return root;
    }
}

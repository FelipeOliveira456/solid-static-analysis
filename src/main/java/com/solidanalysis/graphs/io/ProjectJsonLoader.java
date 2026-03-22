package com.solidanalysis.graphs.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.graphs.GraphObjectMapper;
import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.PlacedArtifact;
import com.solidanalysis.graphs.model.ParsedProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Loads every scanner {@code *.json} from a project output directory into {@link AstArtifact}s. */
public final class ProjectJsonLoader {

    private ProjectJsonLoader() {}

    /**
     * Top-level segments under {@code projectDirectory} that are not Etapa 1 AST JSON trees (Etapa
     * 2–5 outputs).
     */
    private static boolean isReservedTopLevelSegment(String name) {
        return name.equals("graphs")
                || name.equals("algorithms")
                || name.equals("scoring")
                || name.equals("results");
    }

    static boolean isScannerArtifactJson(Path projectDirectory, Path file) {
        Path rel = projectDirectory.relativize(file.toAbsolutePath().normalize());
        if (rel.getNameCount() == 0) {
            return false;
        }
        return !isReservedTopLevelSegment(rel.getName(0).toString());
    }

    public static List<AstArtifact> loadArtifacts(Path projectDirectory) throws IOException {
        ObjectMapper mapper = GraphObjectMapper.create();
        List<AstArtifact> out = new ArrayList<>();
        if (!Files.isDirectory(projectDirectory)) {
            throw new IOException("Not a directory: " + projectDirectory);
        }
        Path abs = projectDirectory.toAbsolutePath().normalize();
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(abs)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> isScannerArtifactJson(abs, p))
                    .forEach(jsonFiles::add);
        }
        jsonFiles.sort(Comparator.naturalOrder());
        for (Path p : jsonFiles) {
            out.add(mapper.readValue(p.toFile(), AstArtifact.class));
        }
        return out;
    }

    public static List<PlacedArtifact> loadPlacedArtifacts(Path projectDirectory) throws IOException {
        ObjectMapper mapper = GraphObjectMapper.create();
        if (!Files.isDirectory(projectDirectory)) {
            throw new IOException("Not a directory: " + projectDirectory);
        }
        Path abs = projectDirectory.toAbsolutePath().normalize();
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(abs)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .filter(p -> isScannerArtifactJson(abs, p))
                    .forEach(jsonFiles::add);
        }
        jsonFiles.sort(Comparator.naturalOrder());
        List<PlacedArtifact> placed = new ArrayList<>();
        for (Path p : jsonFiles) {
            Path rel = abs.relativize(p);
            Path parent = rel.getParent();
            Path relDir = parent == null ? Path.of("") : parent;
            AstArtifact a = mapper.readValue(p.toFile(), AstArtifact.class);
            placed.add(new PlacedArtifact(relDir, a));
        }
        return placed;
    }

    public static ParsedProject loadParsed(Path projectDirectory) throws IOException {
        return ParsedProject.fromPlaced(loadPlacedArtifacts(projectDirectory));
    }
}

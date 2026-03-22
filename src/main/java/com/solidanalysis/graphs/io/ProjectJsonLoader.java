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

/**
 * Loads scanner {@code *.json} from {@code <project>/ast/} into {@link AstArtifact}s and {@link
 * PlacedArtifact}s.
 */
public final class ProjectJsonLoader {

    private ProjectJsonLoader() {}

    public static List<AstArtifact> loadArtifacts(Path projectDirectory) throws IOException {
        ObjectMapper mapper = GraphObjectMapper.create();
        List<AstArtifact> out = new ArrayList<>();
        if (!Files.isDirectory(projectDirectory)) {
            throw new IOException("Not a directory: " + projectDirectory);
        }
        Path ast = ProjectOutputLayout.astDirectory(projectDirectory);
        if (!Files.isDirectory(ast)) {
            return out;
        }
        Path absAst = ast.toAbsolutePath().normalize();
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(absAst)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
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
        Path ast = ProjectOutputLayout.astDirectory(projectDirectory);
        if (!Files.isDirectory(ast)) {
            return List.of();
        }
        Path absAst = ast.toAbsolutePath().normalize();
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(absAst)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .forEach(jsonFiles::add);
        }
        jsonFiles.sort(Comparator.naturalOrder());
        List<PlacedArtifact> placed = new ArrayList<>();
        for (Path p : jsonFiles) {
            Path rel = absAst.relativize(p);
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

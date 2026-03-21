package com.solidanalysis.graphs.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solidanalysis.graphs.GraphObjectMapper;
import com.solidanalysis.graphs.model.AstArtifact;
import com.solidanalysis.graphs.model.ParsedProject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Loads every {@code *.json} file from a project output directory into {@link AstArtifact}s. */
public final class ProjectJsonLoader {

    private ProjectJsonLoader() {}

    public static List<AstArtifact> loadArtifacts(Path projectDirectory) throws IOException {
        ObjectMapper mapper = GraphObjectMapper.create();
        List<AstArtifact> out = new ArrayList<>();
        if (!Files.isDirectory(projectDirectory)) {
            throw new IOException("Not a directory: " + projectDirectory);
        }
        try (Stream<Path> stream = Files.list(projectDirectory)) {
            List<Path> jsonFiles =
                    stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                            .sorted()
                            .toList();
            for (Path p : jsonFiles) {
                out.add(mapper.readValue(p.toFile(), AstArtifact.class));
            }
        }
        return out;
    }

    public static ParsedProject loadParsed(Path projectDirectory) throws IOException {
        return ParsedProject.fromArtifacts(loadArtifacts(projectDirectory));
    }
}

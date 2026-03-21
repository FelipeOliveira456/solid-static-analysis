package com.solidanalysis.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.solidanalysis.scanner.model.AstArtifact;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes {@link AstArtifact} instances as UTF-8 JSON files.
 */
public final class ArtifactJsonWriter {

    private final ObjectMapper mapper;

    public ArtifactJsonWriter() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializes the artifact to the given path (parent directories are created if needed).
     */
    public void write(AstArtifact artifact, Path targetJsonFile) throws IOException {
        Path parent = targetJsonFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        byte[] data = mapper.writeValueAsString(artifact).getBytes(StandardCharsets.UTF_8);
        Files.write(targetJsonFile, data);
    }
}

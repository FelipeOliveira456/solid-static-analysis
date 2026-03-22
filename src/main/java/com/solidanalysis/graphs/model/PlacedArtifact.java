package com.solidanalysis.graphs.model;

import java.nio.file.Path;
import java.util.Objects;

/**
 * One parsed compilation unit with the path to its Etapa 1 JSON relative to the project output
 * directory (parent folder of the {@code .json}), used to mirror source package layout under
 * {@code graphs/}, {@code algorithms/}, {@code scoring/}, and {@code results/}.
 */
public record PlacedArtifact(Path relativeOutputDir, AstArtifact artifact) {

    public PlacedArtifact {
        Objects.requireNonNull(relativeOutputDir, "relativeOutputDir");
        Objects.requireNonNull(artifact, "artifact");
    }

    /** Stable key for maps when the same simple name may appear under different directories. */
    public String slotKey() {
        String rel = relativeOutputDir.toString().replace('\\', '/');
        String name = simpleTypeName();
        return rel.isEmpty() ? name : rel + "/" + name;
    }

    public String simpleTypeName() {
        if (artifact.primaryType() == null || artifact.primaryType().name() == null) {
            return "";
        }
        return artifact.primaryType().name();
    }
}

package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Root JSON DTO for one parsed {@code .java} source file.
 */
public class AstArtifact {

    @JsonProperty("sourceFile")
    private String sourceFile;

    @JsonProperty("primaryType")
    private TypeSummary primaryType;

    public AstArtifact() {}

    public AstArtifact(String sourceFile, TypeSummary primaryType) {
        this.sourceFile = sourceFile;
        this.primaryType = primaryType;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public TypeSummary getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(TypeSummary primaryType) {
        this.primaryType = primaryType;
    }
}

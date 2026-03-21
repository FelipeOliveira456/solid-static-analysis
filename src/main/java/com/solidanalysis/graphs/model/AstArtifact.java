package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AstArtifact(String sourceFile, TypeSummary primaryType) {}

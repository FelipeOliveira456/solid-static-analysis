package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FieldAccessSummary(String fieldName, String ownerClass, String accessType) {}

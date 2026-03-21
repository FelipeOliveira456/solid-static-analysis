package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MethodCallSummary(
        String expression, boolean resolved, String declaringType, String signature) {}

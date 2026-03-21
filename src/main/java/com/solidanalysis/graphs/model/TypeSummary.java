package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TypeSummary(
        String kind,
        String name,
        boolean abstractType,
        String superclass,
        List<String> implementedInterfaces,
        List<FieldSummary> fields,
        List<MethodSummary> methods) {

    @com.fasterxml.jackson.annotation.JsonProperty("abstract")
    public boolean abstractType() {
        return abstractType;
    }
}

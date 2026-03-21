package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Formal parameter of a method.
 */
public class ParameterSummary {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    public ParameterSummary() {}

    public ParameterSummary(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

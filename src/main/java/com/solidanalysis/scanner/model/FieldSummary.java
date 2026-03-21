package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Declared field on a type.
 */
public class FieldSummary {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    public FieldSummary() {}

    public FieldSummary(String name, String type) {
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

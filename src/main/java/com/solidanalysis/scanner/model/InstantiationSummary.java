package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One object creation expression ({@code new Type(...)}) inside a method body. */
public class InstantiationSummary {

    @JsonProperty("type")
    private String type;

    @JsonProperty("line")
    private Integer line;

    public InstantiationSummary() {}

    public InstantiationSummary(String type, Integer line) {
        this.type = type;
        this.line = line;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}

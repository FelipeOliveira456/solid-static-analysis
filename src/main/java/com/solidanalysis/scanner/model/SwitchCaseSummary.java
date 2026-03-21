package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** One arm of a {@code switch} (labels joined when multiple per entry). */
public class SwitchCaseSummary {

    @JsonProperty("label")
    private String label;

    @JsonProperty("line")
    private Integer line;

    public SwitchCaseSummary() {}

    public SwitchCaseSummary(String label, Integer line) {
        this.label = label;
        this.line = line;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}

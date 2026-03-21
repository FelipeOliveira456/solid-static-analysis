package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A control-flow structure found in a method body (if, loops, switch, try, etc.).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ControlFlowStatementSummary {

    @JsonProperty("kind")
    private String kind;

    /** Condition, selector, or other discriminant text (e.g. catch parameter type). */
    @JsonProperty("condition")
    private String condition;

    @JsonProperty("line")
    private Integer line;

    public ControlFlowStatementSummary() {}

    public ControlFlowStatementSummary(String kind, String condition, Integer line) {
        this.kind = kind;
        this.condition = condition;
        this.line = line;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }
}

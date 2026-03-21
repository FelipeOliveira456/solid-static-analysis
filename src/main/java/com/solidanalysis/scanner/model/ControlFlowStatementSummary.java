package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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

    /** First line of the {@code then} / loop body (if, while, for, foreach). */
    @JsonProperty("thenLine")
    private Integer thenLine;

    /** First line of the {@code else} branch; omitted when absent. */
    @JsonProperty("elseLine")
    private Integer elseLine;

    /** Last line of the whole construct (closing brace / statement end). */
    @JsonProperty("endLine")
    private Integer endLine;

    /** Present for {@code switch}: each case label (or {@code default}) and its line. */
    @JsonProperty("cases")
    private List<SwitchCaseSummary> cases;

    /**
     * When {@code kind} is {@code if} and the {@code else} branch is {@code else if (...)}: the
     * nested {@code if} (possibly with further {@code chainedElseIf}), so the chain is one
     * logical construct with mutually exclusive branches.
     */
    @JsonProperty("chainedElseIf")
    private ControlFlowStatementSummary chainedElseIf;

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

    public Integer getThenLine() {
        return thenLine;
    }

    public void setThenLine(Integer thenLine) {
        this.thenLine = thenLine;
    }

    public Integer getElseLine() {
        return elseLine;
    }

    public void setElseLine(Integer elseLine) {
        this.elseLine = elseLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    public void setEndLine(Integer endLine) {
        this.endLine = endLine;
    }

    public List<SwitchCaseSummary> getCases() {
        return cases;
    }

    public void setCases(List<SwitchCaseSummary> cases) {
        this.cases = cases;
    }

    public ControlFlowStatementSummary getChainedElseIf() {
        return chainedElseIf;
    }

    public void setChainedElseIf(ControlFlowStatementSummary chainedElseIf) {
        this.chainedElseIf = chainedElseIf;
    }
}

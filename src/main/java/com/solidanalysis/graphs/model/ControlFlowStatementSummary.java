package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Control-flow node extracted from the AST (if, loops, switch, etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ControlFlowStatementSummary(
        String kind,
        String condition,
        Integer line,
        Integer thenLine,
        Integer elseLine,
        Integer endLine,
        List<SwitchCaseSummary> cases,
        ControlFlowStatementSummary chainedElseIf) {}

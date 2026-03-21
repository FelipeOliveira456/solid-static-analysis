package com.solidanalysis.graphs.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MethodSummary(
        String name,
        String returnType,
        List<ParameterSummary> parameters,
        List<MethodCallSummary> methodCalls,
        List<ControlFlowStatementSummary> controlFlowStatements,
        List<FieldAccessSummary> fieldAccesses,
        List<InstantiationSummary> instantiations) {}

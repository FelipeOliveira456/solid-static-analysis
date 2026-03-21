package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Declared method on a type (instance or static), excluding constructors unless added separately.
 */
public class MethodSummary {

    @JsonProperty("name")
    private String name;

    @JsonProperty("returnType")
    private String returnType;

    @JsonProperty("parameters")
    private List<ParameterSummary> parameters = new ArrayList<>();

    @JsonProperty("methodCalls")
    private List<MethodCallSummary> methodCalls = new ArrayList<>();

    /** Control-flow nodes in method body order (if, loops, switch, try, catch, …). */
    @JsonProperty("controlFlowStatements")
    private List<ControlFlowStatementSummary> controlFlowStatements = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<ParameterSummary> getParameters() {
        return parameters;
    }

    public void setParameters(List<ParameterSummary> parameters) {
        this.parameters = parameters;
    }

    public List<MethodCallSummary> getMethodCalls() {
        return methodCalls;
    }

    public void setMethodCalls(List<MethodCallSummary> methodCalls) {
        this.methodCalls = methodCalls;
    }

    public List<ControlFlowStatementSummary> getControlFlowStatements() {
        return controlFlowStatements;
    }

    public void setControlFlowStatements(List<ControlFlowStatementSummary> controlFlowStatements) {
        this.controlFlowStatements = controlFlowStatements;
    }
}

package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A method call site found inside a method body.
 */
public class MethodCallSummary {

    @JsonProperty("expression")
    private String expression;

    @JsonProperty("resolved")
    private boolean resolved;

    @JsonProperty("declaringType")
    private String declaringType;

    @JsonProperty("signature")
    private String signature;

    public MethodCallSummary() {}

    public MethodCallSummary(String expression, boolean resolved, String declaringType, String signature) {
        this.expression = expression;
        this.resolved = resolved;
        this.declaringType = declaringType;
        this.signature = signature;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    public void setDeclaringType(String declaringType) {
        this.declaringType = declaringType;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}

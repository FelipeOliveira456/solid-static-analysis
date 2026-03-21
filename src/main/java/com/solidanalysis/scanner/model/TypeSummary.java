package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Summary of the primary class or interface declared in a compilation unit.
 */
public class TypeSummary {

    @JsonProperty("kind")
    private String kind;

    @JsonProperty("name")
    private String name;

    @JsonProperty("abstract")
    private boolean abstractType;

    @JsonProperty("superclass")
    private String superclass;

    @JsonProperty("implementedInterfaces")
    private List<String> implementedInterfaces = new ArrayList<>();

    @JsonProperty("fields")
    private List<FieldSummary> fields = new ArrayList<>();

    @JsonProperty("methods")
    private List<MethodSummary> methods = new ArrayList<>();

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("abstract")
    public boolean isAbstractType() {
        return abstractType;
    }

    public void setAbstractType(boolean abstractType) {
        this.abstractType = abstractType;
    }

    public String getSuperclass() {
        return superclass;
    }

    public void setSuperclass(String superclass) {
        this.superclass = superclass;
    }

    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public void setImplementedInterfaces(List<String> implementedInterfaces) {
        this.implementedInterfaces = implementedInterfaces;
    }

    public List<FieldSummary> getFields() {
        return fields;
    }

    public void setFields(List<FieldSummary> fields) {
        this.fields = fields;
    }

    public List<MethodSummary> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodSummary> methods) {
        this.methods = methods;
    }
}

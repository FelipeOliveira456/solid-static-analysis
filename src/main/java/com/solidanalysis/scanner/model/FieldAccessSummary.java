package com.solidanalysis.scanner.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A resolved read or write access to a field (instance or static). */
public class FieldAccessSummary {

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("ownerClass")
    private String ownerClass;

    /** {@code "read"} or {@code "write"} (assignment target). */
    @JsonProperty("accessType")
    private String accessType;

    public FieldAccessSummary() {}

    public FieldAccessSummary(String fieldName, String ownerClass, String accessType) {
        this.fieldName = fieldName;
        this.ownerClass = ownerClass;
        this.accessType = accessType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOwnerClass() {
        return ownerClass;
    }

    public void setOwnerClass(String ownerClass) {
        this.ownerClass = ownerClass;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }
}

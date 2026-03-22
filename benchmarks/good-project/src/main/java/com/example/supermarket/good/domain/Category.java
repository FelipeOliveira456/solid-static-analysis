package com.example.supermarket.good.domain;

public class Category {

    private final String code;
    private final String label;

    public Category(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}

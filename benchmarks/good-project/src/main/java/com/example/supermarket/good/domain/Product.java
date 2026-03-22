package com.example.supermarket.good.domain;

public class Product {

    private final String id;
    private final String name;
    private final Money unitPrice;

    public Product(String id, String name, Money unitPrice) {
        this.id = id;
        this.name = name;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }
}

package com.example.supermarket.good.domain;

public class OrderLine {

    private final Product product;
    private final int quantity;
    private final Money lineTotal;

    public OrderLine(Product product, int quantity, Money lineTotal) {
        this.product = product;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public Money getLineTotal() {
        return lineTotal;
    }
}

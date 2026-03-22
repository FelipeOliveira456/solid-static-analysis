package com.example.supermarket.good.domain;

public class CartLine {

    private final String productId;
    private final int quantity;

    public CartLine(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}

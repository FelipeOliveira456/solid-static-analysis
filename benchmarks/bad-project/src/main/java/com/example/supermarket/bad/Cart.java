package com.example.supermarket.bad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(String productId, int quantity) {
        items.add(new CartItem(productId, quantity));
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}

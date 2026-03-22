package com.example.supermarket.good.repository;

import java.util.HashMap;
import java.util.Map;

public class InMemoryInventory implements InventoryPort {

    private final Map<String, Integer> stock = new HashMap<>();

    public void seed(String productId, int qty) {
        stock.put(productId, qty);
    }

    @Override
    public int available(String productId) {
        return stock.getOrDefault(productId, 0);
    }

    @Override
    public void reserve(String productId, int quantity) {
        stock.merge(productId, -quantity, Integer::sum);
    }
}

package com.example.supermarket.good.repository;

public interface InventoryPort {

    int available(String productId);

    void reserve(String productId, int quantity);
}

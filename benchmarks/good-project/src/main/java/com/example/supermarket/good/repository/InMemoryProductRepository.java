package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Product;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> data = new HashMap<>();

    public void put(Product p) {
        data.put(p.getId(), p);
    }

    @Override
    public Optional<Product> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }
}

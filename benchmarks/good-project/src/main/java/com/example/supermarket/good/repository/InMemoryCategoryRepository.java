package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Category;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<String, Category> data = new HashMap<>();

    public void put(Category c) {
        data.put(c.getCode(), c);
    }

    @Override
    public Optional<Category> findByCode(String code) {
        return Optional.ofNullable(data.get(code));
    }
}

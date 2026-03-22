package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Category;
import java.util.Optional;

public interface CategoryRepository {

    Optional<Category> findByCode(String code);
}

package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Product;
import java.util.Optional;

public interface ProductRepository {

    Optional<Product> findById(String id);
}

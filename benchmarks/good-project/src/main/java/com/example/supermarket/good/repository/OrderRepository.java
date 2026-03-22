package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Order;

public interface OrderRepository {

    void save(Order order);
}

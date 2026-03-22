package com.example.supermarket.good.repository;

import com.example.supermarket.good.domain.Order;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InMemoryOrderRepository implements OrderRepository {

    private final List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
    }

    public List<Order> snapshot() {
        return Collections.unmodifiableList(orders);
    }
}

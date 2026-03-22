package com.example.supermarket.good.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Order {

    private final Customer customer;
    private final List<OrderLine> lines = new ArrayList<>();

    public Order(Customer customer) {
        this.customer = customer;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}

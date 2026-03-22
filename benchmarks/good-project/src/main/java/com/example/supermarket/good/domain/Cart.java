package com.example.supermarket.good.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {

    private final List<CartLine> lines = new ArrayList<>();

    public void addLine(CartLine line) {
        lines.add(line);
    }

    public List<CartLine> getLines() {
        return Collections.unmodifiableList(lines);
    }
}

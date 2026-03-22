package com.example.supermarket.bad;

import java.util.HashMap;
import java.util.Map;

public final class Inventory {

    private static final Map<String, Integer> STOCK = new HashMap<>();

    private Inventory() {}

    public static void seed(String productId, int qty) {
        STOCK.put(productId, qty);
    }

    public static void adjust(String productId, int delta) {
        STOCK.merge(productId, delta, Integer::sum);
    }
}

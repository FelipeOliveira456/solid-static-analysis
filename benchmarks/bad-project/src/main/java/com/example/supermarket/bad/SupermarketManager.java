package com.example.supermarket.bad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central god class: handles sales, inventory, reporting, and discounts in one place.
 */
public class SupermarketManager {

    private final Map<String, Product> products = new HashMap<>();
    private final List<Order> orders = new ArrayList<>();

    // SOLID-VIOLATION: S
    public void registerProduct(String id, String name, double price) {
        products.put(id, new Product(id, name, price));
    }

    // SOLID-VIOLATION: S
    public Receipt checkout(Cart cart, Customer customer, Payment payment) {
        Order order = new Order(customer, cart);
        orders.add(order);
        adjustInventoryForCart(cart);
        return new Receipt(order, payment);
    }

    // SOLID-VIOLATION: S
    private void adjustInventoryForCart(Cart cart) {
        for (CartItem item : cart.getItems()) {
            Inventory.adjust(item.getProductId(), -item.getQuantity());
        }
    }

    // SOLID-VIOLATION: O
    public double applyLoyaltyDiscount(Customer c, double total) {
        if (c.getTier().equals("GOLD")) {
            return total * 0.9;
        }
        if (c.getTier().equals("SILVER")) {
            return total * 0.95;
        }
        return total;
    }

    // SOLID-VIOLATION: D
    public void emailReceipt(Receipt receipt) {
        new Emailer().send(receipt.toString());
    }
}

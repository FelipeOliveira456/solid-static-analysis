package com.example.supermarket.bad;

/**
 * Pretends to be a specialized calculator but inherits the entire supermarket manager.
 */
// SOLID-VIOLATION: L
public class DiscountCalculator extends SupermarketManager {

    public double stackCoupons(double base, Discount d1, Discount d2) {
        return base * (1 - d1.getPercent() / 100) * (1 - d2.getPercent() / 100);
    }
}

package com.example.supermarket.good.service;

import com.example.supermarket.good.domain.Customer;
import com.example.supermarket.good.domain.Money;

public class LoyaltyDiscountService {

    public Money applyTierDiscount(Customer customer, Money subtotal) {
        return switch (customer.getLoyaltyTier()) {
            case "GOLD" -> subtotal.multiply(0.9);
            case "SILVER" -> subtotal.multiply(0.95);
            default -> subtotal;
        };
    }
}

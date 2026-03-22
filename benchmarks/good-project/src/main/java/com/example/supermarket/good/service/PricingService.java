package com.example.supermarket.good.service;

import com.example.supermarket.good.domain.Money;
import com.example.supermarket.good.domain.Product;

public class PricingService {

    public Money lineTotal(Product product, int quantity) {
        return new Money(
                product.getUnitPrice().getAmount().multiply(java.math.BigDecimal.valueOf(quantity)));
    }
}

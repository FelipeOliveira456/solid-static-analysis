package com.example.supermarket.good.service;

import com.example.supermarket.good.domain.Money;
import java.math.BigDecimal;

public class TaxService {

    private final BigDecimal rate;

    public TaxService(BigDecimal rate) {
        this.rate = rate;
    }

    public Money apply(Money subtotal) {
        BigDecimal tax = subtotal.getAmount().multiply(rate);
        return new Money(subtotal.getAmount().add(tax));
    }
}

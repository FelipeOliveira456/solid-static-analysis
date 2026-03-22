package com.example.supermarket.good.usecase;

import com.example.supermarket.good.domain.Money;

public class PromotionApplicationUseCase {

    public Money applyPercentOff(Money base, double percentOff) {
        double factor = 1.0 - percentOff / 100.0;
        return base.multiply(factor);
    }
}

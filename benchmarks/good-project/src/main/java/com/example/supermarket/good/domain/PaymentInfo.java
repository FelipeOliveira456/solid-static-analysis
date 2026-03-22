package com.example.supermarket.good.domain;

public class PaymentInfo {

    private final String method;
    private final Money amount;

    public PaymentInfo(String method, Money amount) {
        this.method = method;
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public Money getAmount() {
        return amount;
    }
}

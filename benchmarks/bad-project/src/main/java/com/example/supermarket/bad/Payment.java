package com.example.supermarket.bad;

public class Payment {

    private final String method;
    private final double amount;

    public Payment(String method, double amount) {
        this.method = method;
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public double getAmount() {
        return amount;
    }
}

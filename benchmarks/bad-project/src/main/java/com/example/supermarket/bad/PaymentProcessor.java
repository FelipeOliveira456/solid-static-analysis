package com.example.supermarket.bad;

public class PaymentProcessor {

    // SOLID-VIOLATION: O
    public boolean authorize(Payment payment) {
        switch (payment.getMethod()) {
            case "CARD":
                return payment.getAmount() < 500;
            case "CASH":
                return true;
            default:
                return false;
        }
    }
}

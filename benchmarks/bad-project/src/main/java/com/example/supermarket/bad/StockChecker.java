package com.example.supermarket.bad;

public class StockChecker {

    // SOLID-VIOLATION: O
    public String statusFor(String productId) {
        switch (productId) {
            case "A":
                return "OK";
            case "B":
                return "LOW";
            default:
                return "UNKNOWN";
        }
    }
}

package com.example.supermarket.good.domain;

public class Customer {

    private final String id;
    private String loyaltyTier;

    public Customer(String id) {
        this.id = id;
        this.loyaltyTier = "STANDARD";
    }

    public String getId() {
        return id;
    }

    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }
}

package com.example.supermarket.bad;

public class Customer {

    private final String id;
    private String tier;

    public Customer(String id) {
        this.id = id;
        this.tier = "BRONZE";
    }

    public String getId() {
        return id;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }
}

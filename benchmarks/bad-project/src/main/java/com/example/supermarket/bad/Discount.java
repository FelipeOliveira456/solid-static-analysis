package com.example.supermarket.bad;

public class Discount {

    private final String code;
    private final double percent;

    public Discount(String code, double percent) {
        this.code = code;
        this.percent = percent;
    }

    public String getCode() {
        return code;
    }

    public double getPercent() {
        return percent;
    }
}

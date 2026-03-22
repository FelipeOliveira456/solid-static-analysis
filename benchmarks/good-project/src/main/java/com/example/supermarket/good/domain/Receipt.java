package com.example.supermarket.good.domain;

import java.util.List;

public class Receipt {

    private final Order order;
    private final PaymentInfo payment;
    private final Money grandTotal;

    public Receipt(Order order, PaymentInfo payment, Money grandTotal) {
        this.order = order;
        this.payment = payment;
        this.grandTotal = grandTotal;
    }

    public Order getOrder() {
        return order;
    }

    public PaymentInfo getPayment() {
        return payment;
    }

    public Money getGrandTotal() {
        return grandTotal;
    }

    public List<OrderLine> getLines() {
        return order.getLines();
    }
}

package com.example.supermarket.bad;

public class Receipt {

    private final Order order;
    private final Payment payment;

    public Receipt(Order order, Payment payment) {
        this.order = order;
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "Receipt{order=" + order.getCustomer().getId() + ", paid=" + payment.getAmount() + "}";
    }
}

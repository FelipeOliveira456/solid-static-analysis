package com.example.supermarket.good.service;

import com.example.supermarket.good.domain.OrderLine;
import com.example.supermarket.good.domain.Receipt;
import java.util.stream.Collectors;

public class ReceiptFormatter {

    public String toPlainText(Receipt receipt) {
        String lines =
                receipt.getLines().stream()
                        .map(OrderLine::getProduct)
                        .map(p -> p.getId() + " " + p.getName())
                        .collect(Collectors.joining(", "));
        return "Receipt for customer "
                + receipt.getOrder().getCustomer().getId()
                + " items="
                + lines
                + " total="
                + receipt.getGrandTotal().getAmount();
    }
}

package com.example.supermarket.good.usecase;

import com.example.supermarket.good.domain.Cart;
import com.example.supermarket.good.domain.CartLine;
import com.example.supermarket.good.repository.InventoryPort;

public class StockReservationUseCase {

    private final InventoryPort inventory;

    public StockReservationUseCase(InventoryPort inventory) {
        this.inventory = inventory;
    }

    public void reserveForCart(Cart cart) {
        for (CartLine line : cart.getLines()) {
            inventory.reserve(line.getProductId(), line.getQuantity());
        }
    }
}

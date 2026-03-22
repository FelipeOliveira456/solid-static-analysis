package com.example.supermarket.good.usecase;

import com.example.supermarket.good.domain.Cart;
import com.example.supermarket.good.domain.Customer;
import com.example.supermarket.good.domain.Money;
import com.example.supermarket.good.domain.Order;
import com.example.supermarket.good.domain.OrderLine;
import com.example.supermarket.good.domain.PaymentInfo;
import com.example.supermarket.good.domain.Product;
import com.example.supermarket.good.domain.Receipt;
import com.example.supermarket.good.repository.OrderRepository;
import com.example.supermarket.good.repository.ProductRepository;
import com.example.supermarket.good.service.LoyaltyDiscountService;
import com.example.supermarket.good.service.PricingService;
import com.example.supermarket.good.service.TaxService;
import java.math.BigDecimal;

public class CheckoutUseCase {

    private final ProductRepository products;
    private final OrderRepository orders;
    private final PricingService pricing;
    private final TaxService tax;
    private final LoyaltyDiscountService loyalty;
    private final StockReservationUseCase stock;

    public CheckoutUseCase(
            ProductRepository products,
            OrderRepository orders,
            PricingService pricing,
            TaxService tax,
            LoyaltyDiscountService loyalty,
            StockReservationUseCase stock) {
        this.products = products;
        this.orders = orders;
        this.pricing = pricing;
        this.tax = tax;
        this.loyalty = loyalty;
        this.stock = stock;
    }

    public Receipt checkout(Customer customer, Cart cart, PaymentInfo payment) {
        Order order = new Order(customer);
        Money subtotal = new Money(BigDecimal.ZERO);
        for (var line : cart.getLines()) {
            Product p =
                    products.findById(line.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Unknown product"));
            Money lt = pricing.lineTotal(p, line.getQuantity());
            order.addLine(new OrderLine(p, line.getQuantity(), lt));
            subtotal = subtotal.add(lt);
        }
        Money afterLoyalty = loyalty.applyTierDiscount(customer, subtotal);
        Money withTax = tax.apply(afterLoyalty);
        stock.reserveForCart(cart);
        orders.save(order);
        return new Receipt(order, payment, withTax);
    }
}

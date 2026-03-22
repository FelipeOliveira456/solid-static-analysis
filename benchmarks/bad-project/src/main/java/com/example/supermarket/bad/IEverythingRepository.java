package com.example.supermarket.bad;

// SOLID-VIOLATION: I
public interface IEverythingRepository {

    void saveProduct(Product p);

    void saveCustomer(Customer c);

    void saveOrder(Order o);

    void backupToDisk();

    void sendAnalyticsEvent(String name);
}

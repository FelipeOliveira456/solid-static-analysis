package com.example.supermarket.bad;

public class InMemoryRepository implements IEverythingRepository {

    @Override
    public void saveProduct(Product p) {}

    @Override
    public void saveCustomer(Customer c) {}

    @Override
    public void saveOrder(Order o) {}

    @Override
    public void backupToDisk() {}

    @Override
    public void sendAnalyticsEvent(String name) {}
}

package com.ms.matchingengine;

public class OrderIdGenerator {
    private int counter = 1;

    public OrderIdGenerator() {
        this.counter = counter;
    }

    public String generateId() {
        return "ID_" + counter++;
    }
}

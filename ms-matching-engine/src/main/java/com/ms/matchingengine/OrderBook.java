package com.ms.matchingengine;

import java.util.*;

public class OrderBook {
    TreeMap<Double, LinkedList<Order>> bids = new TreeMap<>(Comparator.reverseOrder());
    TreeMap<Double, LinkedList<Order>> asks = new TreeMap<>();
    Map<String, Order> orderMap = new HashMap<>();

    // The TreeMap property is being used to maintain de Order in our book, decrescent for bids and crescent, the LinkedList is to maintain FIFO rule
    // and the HashMap property will be used to do the cancellation of orders more efficiently by looking for its Id in the book.

    public TreeMap<Double, LinkedList<Order>> getBids() {
        return bids;
    }

    public TreeMap<Double, LinkedList<Order>> getAsks() {
        return asks;
    }

    public Map<String, Order> getOrderMap() {
        return orderMap;
    }
}

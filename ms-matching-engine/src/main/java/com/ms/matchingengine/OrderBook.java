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

    
    public void addLimitOrder(Order order) {
        // Saving the order on the orderMap property to map and use it after for cancelling/modifying the order
        orderMap.put(order.getId(), order);

        // Defining Side (Bids or Asks)
        TreeMap<Double, LinkedList<Order>> targetBook = (order.getSide() == Side.BUY) ? bids : asks;

        // Inserting into the TreeMap mainting the chronological line
        targetBook.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).addLast(order);
    }
}

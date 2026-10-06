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

    public void executeMarketOrder(Order marketOrder) {
        while (marketOrder.getRemainingQty() > 0 && !asks.isEmpty()) {
            TreeMap<Double, LinkedList<Order>> targetBook = (marketOrder.getSide() == Side.BUY) ? asks : bids;

            Double bstPrice = targetBook.firstKey();

            LinkedList<Order> priceQueue = targetBook.get(bstPrice);

            Order restingOrder = priceQueue.peekFirst();

            int negociateTradeQty = Math.min(marketOrder.getRemainingQty(), restingOrder.getRemainingQty());

            marketOrder.reduceremainingQty(negociateTradeQty);
            restingOrder.reduceremainingQty(negociateTradeQty);

            System.out.println("Trade, price: " + bstPrice + ", qty: " + negociateTradeQty);

            if (restingOrder.getRemainingQty() == 0) {
                priceQueue.removeFirst();
                orderMap.remove(restingOrder.getId());
            }

            if (priceQueue.isEmpty()) {
                asks.remove(bstPrice);
            }
        }
    }

    public void printBook() {
        System.out.println("Ordens de Compra    | Ordens de Venda");
        System.out.println("--------------------|-----------------");

        Set<Map.Entry<Double, LinkedList<Order>>> bidEntries = bids.entrySet();
        Set<Map.Entry<Double, LinkedList<Order>>> askEntries = asks.entrySet();

        int maxRows = Math.max(bidEntries.size(), askEntries.size());

        Iterator<Map.Entry<Double, LinkedList<Order>>> bidIterator = bidEntries.iterator();
        Iterator<Map.Entry<Double, LinkedList<Order>>> askIterator = askEntries.iterator();

        for (int i = 0; i < maxRows; i++) {
            String bidOutput = "";
            if (bidIterator.hasNext()) {
                Map.Entry<Double, LinkedList<Order>> entry = bidIterator.next();
                int totalQty = entry.getValue().stream().mapToInt(Order::getRemainingQty).sum();
                bidOutput = totalQty + "@" + entry.getKey();
            }
            String askOutput = "";
            if(askIterator.hasNext()) {
                Map.Entry<Double, LinkedList<Order>> entry = askIterator.next();
                int totalQty = entry.getValue().stream().mapToInt(Order::getRemainingQty).sum();
                askOutput = totalQty + "@" + entry.getKey();
            }
            
            System.out.printf("%-19s | %s%n", bidOutput, askOutput);
        }
        System.out.println();
    }

}

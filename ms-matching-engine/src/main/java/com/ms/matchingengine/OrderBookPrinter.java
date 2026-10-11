package com.ms.matchingengine;

import java.util.*;

public class OrderBookPrinter {
    OrderBook orderBook = new OrderBook();

    public OrderBookPrinter(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public void printBook() {
        System.out.println("\n--------------- ORDERBOOK VISUALIZATON -------------------- ");
        System.out.printf("%-5s | %-20s | %-5s | %-20s%n", "ID", "Ordens de Compra", "ID", "Ordens de Venda");
        System.out.println("----- | -------------------- | ----- | --------------------");

        List<Order> buyOrdersList = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> entry : orderBook.buyOrders.entrySet()) {
            for (Order order : entry.getValue()) {
                buyOrdersList.add(order);
            }
        }

        List<Order> sellOrdersList = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> entry : orderBook.sellOrders.entrySet()) {
            for (Order order : entry.getValue()) {
                sellOrdersList.add(order);
            }
        }

        int maxRows = Math.max(buyOrdersList.size(), sellOrdersList.size());
        for (int i = 0; i < maxRows; i++) {
            String buyText = "";
            String buyId = "";
            if (i < buyOrdersList.size()) {
                Order o = buyOrdersList.get(i);
                buyId = o.getId();
                buyText = o.getRemainingQty() + " @ " + String.format(Locale.US, "%.2f", o.getPrice());
            }

            String sellText = "";
            String sellId = "";
            if (i < sellOrdersList.size()) {
                Order o = sellOrdersList.get(i);
                sellId = o.getId();
                sellText = o.getRemainingQty() + " @ " + String.format(Locale.US, "%.2f", o.getPrice());
            }
            System.out.printf("%-5s | %-20s | %-5s | %-20s%n", buyId, buyText, sellId, sellText);
        }
        System.out.println();
    }
}

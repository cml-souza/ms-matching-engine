package com.ms.matchingengine;

import java.util.*;

public class OrderBook {
    //Buy orders are sorted by highest price first
    TreeMap<Double, LinkedList<Order>> buyOrders = new TreeMap<>(Comparator.reverseOrder());
    // Sell orders are sorted by lowest price first
    TreeMap<Double, LinkedList<Order>> sellOrders = new TreeMap<>();
    Map<String, Order> ordersById = new HashMap<>();

    public TreeMap<Double, LinkedList<Order>> getBuyOrders() {
        return buyOrders;
    }

    public TreeMap<Double, LinkedList<Order>> getSellOrders() {
        return sellOrders;
    }

    public Map<String, Order> getOrdersById() {
        return ordersById;
    }


    public void addOrderToBook(Order order) {
        // Saving the order on the ordersById property to map and use it after for cancelling/modifying the order
        ordersById.put(order.getId(), order);

        TreeMap<Double, LinkedList<Order>> orderBook = (order.getSide() == Side.BUY) ? buyOrders : sellOrders;

        // Orders at the same price follow FIFO priority.
        orderBook.computeIfAbsent(order.getPrice(), k -> new LinkedList<>()).addLast(order);
    }

    public void matchMarketOrder(Order marketOrder) {
        TreeMap<Double, LinkedList<Order>> oppositeBook = (marketOrder.getSide() == Side.BUY) ? sellOrders : buyOrders;

        while (!oppositeBook.isEmpty() && marketOrder.getRemainingQty() > 0) {
            Double bestPrice = oppositeBook.firstKey();

            LinkedList<Order> priceQueue = oppositeBook.get(bestPrice);

            Order restingOrder = priceQueue.peekFirst();

            int executedQuantity = Math.min(marketOrder.getRemainingQty(), restingOrder.getRemainingQty());

            marketOrder.reduceRemainingQty(executedQuantity);
            restingOrder.reduceRemainingQty(executedQuantity);

            System.out.println("Trade, price: " + bestPrice + ", qty: " + executedQuantity);

            // After being consumed the market order is removed from the book
            if (restingOrder.getRemainingQty() == 0) {
                priceQueue.removeFirst();
                ordersById.remove(restingOrder.getId());
            }

            if (priceQueue.isEmpty()) {
                oppositeBook.remove(bestPrice);
            }
        }
    }

    public void printBook() {
        System.out.println("Ordens de Compra    | Ordens de Venda");
        System.out.println("--------------------|-----------------");

        List<String> buyLines = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> buyEntry : buyOrders.entrySet()) {
            double price = buyEntry.getKey();
            for (Order order : buyEntry.getValue()) {
                buyLines.add(order.getRemainingQty() + " @ " + price);
            }
        }
        List<String> sellLines = new ArrayList<>();
        for (Map.Entry<Double, LinkedList<Order>> sellEntry : sellOrders.entrySet()) {
            double price = sellEntry.getKey();
            for (Order order : sellEntry.getValue()) {
                sellLines.add(order.getRemainingQty() + " @ " + price);
            }
        }

        int maxRows = Math.max(buyLines.size(), sellLines.size());
        for (int i = 0; i < maxRows; i++) {
            String buyOutput = (i < buyLines.size() ? buyLines.get(i) : "");
            String sellOutput = (i < sellLines.size() ? sellLines.get(i) : "");
            System.out.printf("%-19s | %s%n", buyOutput, sellOutput);
        }
        System.out.println();
    }

    private void updatePeggedOrderPrice(Order order, double newPrice){
        // When the book changes the pegged order will follow to track the best price
        double oldPrice = order.getPrice();
        Side side = order.getSide();

        TreeMap<Double, LinkedList<Order>> orderBook = (side == Side.BUY) ? buyOrders : sellOrders;
        LinkedList<Order> oldPriceQueue  =  orderBook.get(oldPrice);
        if (oldPriceQueue != null) {
            oldPriceQueue.remove(order);
        }

        if (oldPriceQueue.isEmpty()) {
            orderBook.remove(oldPrice);
        }
        order.setPrice(newPrice);
        orderBook.computeIfAbsent(newPrice, k -> new LinkedList<>()).addLast(order);
    }

    public void updatePeggedOrders() {
        Double bestBid = buyOrders.isEmpty() ? null : buyOrders.firstKey();
        Double bestOffer = sellOrders.isEmpty() ? null : sellOrders.firstKey();

        for (Order order : ordersById.values()) {
            if (order.getType() != OrderType.PEGGED_BID && order.getType() != OrderType.PEGGED_OFFER){
                continue;
            }
            Double newPrice = null;
            if (order.getType() == OrderType.PEGGED_BID && bestBid != null) {
                newPrice = bestBid;
            } else if (order.getType() == OrderType.PEGGED_OFFER && bestOffer != null) {
                newPrice = bestOffer;
            }

            if (newPrice != null && order.getPrice() != newPrice){
                updatePeggedOrderPrice(order, newPrice);
            }
        }
    }

    public boolean cancelOrder(String orderId) {
        Order order = ordersById.get(orderId);
        if (order == null) {
            System.out.println("Order not found: " + orderId);
            return false;
        }

        double price = order.getPrice();
        Side side = order.getSide();

        TreeMap<Double, LinkedList<Order>> orderBook = (side == Side.BUY) ? buyOrders : sellOrders;
        LinkedList<Order> priceQueue = orderBook.get(price);
        if (priceQueue != null) {
            priceQueue.remove(order);
            if (priceQueue.isEmpty()) {
                orderBook.remove(price);
            }
        }
        ordersById.remove(orderId);
        updatePeggedOrders();
        System.out.println("Order cancelled: " + orderId);
        return true;
    }

    public boolean modifyOrder(String orderId, double newPrice, int newQuantity) {
        Order order = ordersById.get(orderId);
        if (order == null) {
            System.out.println("Order not found: " + orderId);
            return false;
        }

        double oldPrice = order.getPrice();
        Side side = order.getSide();
        TreeMap<Double, LinkedList<Order>> orderBook = (side == Side.BUY) ? buyOrders : sellOrders;

        if (oldPrice != newPrice) {
            LinkedList<Order> oldPriceQueue = orderBook.get(oldPrice);
            if (oldPriceQueue != null){
                oldPriceQueue.remove(oldPrice);
                if (oldPriceQueue.isEmpty()){
                    orderBook.remove(oldPrice);
                }
            }
            order.setPrice(newPrice);
            orderBook.computeIfAbsent(newPrice, k -> new LinkedList<>()).addLast(order);
        }
        order.setQuantity(newQuantity);
        updatePeggedOrders();

        System.out.println("Order modified: " + orderId);
        return true;
    }
}

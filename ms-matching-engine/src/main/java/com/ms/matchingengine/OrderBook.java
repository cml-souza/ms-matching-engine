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
        updatePeggedOrders();
    }

    private void updatePeggedOrderPrice(Order order, double newPrice){
        // When the book changes the pegged order will follow to track the best price
        double oldPrice = order.getPrice();
        Side side = order.getSide();

        TreeMap<Double, LinkedList<Order>> orderBook = (side == Side.BUY) ? buyOrders : sellOrders;
        LinkedList<Order> oldPriceQueue  =  orderBook.get(oldPrice);
        if (oldPriceQueue != null) {
            oldPriceQueue.remove(order);
            if (oldPriceQueue.isEmpty()) {
                orderBook.remove(oldPrice);
            }
        }
        order.setPrice(newPrice);
        orderBook.computeIfAbsent(newPrice, k -> new LinkedList<>()).addLast(order);
    }

    public void updatePeggedOrders() {
        Double bestBid = nonPeggedBestPrice(buyOrders);
        Double bestOffer = nonPeggedBestPrice(sellOrders);

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

    private Double nonPeggedBestPrice(TreeMap<Double, LinkedList<Order>> orderBook) {
        for (Map.Entry<Double, LinkedList<Order>> priceEntry : orderBook.entrySet()) {
            boolean hasNonPegged = priceEntry.getValue().stream().anyMatch(order -> order.getType() != OrderType.PEGGED_BID && order.getType() != OrderType.PEGGED_OFFER);
            if (hasNonPegged) {
                return priceEntry.getKey();
            }
        }
        return null;
    }

    public boolean cancelOrder(String orderId) {
        Order order = ordersById.remove(orderId);
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

        if (order.getType() == OrderType.LIMIT) {
            double oldPrice = order.getPrice();
            Side side = order.getSide();
            TreeMap<Double, LinkedList<Order>> orderBook = (side == Side.BUY) ? buyOrders : sellOrders;

            if (oldPrice != newPrice) {
                LinkedList<Order> oldPriceQueue = orderBook.get(oldPrice);
                if (oldPriceQueue != null){
                    oldPriceQueue.remove(order);
                    if (oldPriceQueue.isEmpty()){
                        orderBook.remove(oldPrice);
                    }
                }
                order.setPrice(newPrice);
                orderBook.computeIfAbsent(newPrice, k -> new LinkedList<>()).addLast(order);
            }
        }
        order.setQuantity(newQuantity);
        updatePeggedOrders();

        System.out.println("Order modified: " + orderId);
        return true;
    }
}

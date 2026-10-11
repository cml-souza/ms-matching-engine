package com.ms.matchingengine;

import java.util.LinkedList;
import java.util.TreeMap;

public class TradeExecutor {
    OrderBook orderBook = new OrderBook();

    public TradeExecutor(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public void matchMarketOrder(Order marketOrder) {
        TreeMap<Double, LinkedList<Order>> oppositeBook = (marketOrder.getSide() == Side.BUY) ? orderBook.sellOrders : orderBook.buyOrders;

        if (oppositeBook.isEmpty()) {
            System.out.println("Market order rejected: No liquidity available in the order book.");
            return;
        }

        while (!oppositeBook.isEmpty() && marketOrder.getRemainingQty() > 0) {
            Double bestPrice = oppositeBook.firstKey();

            LinkedList<Order> priceQueue = oppositeBook.get(bestPrice);

            Order restingOrder = priceQueue.peekFirst();

            int executedQuantity = Math.min(marketOrder.getRemainingQty(), restingOrder.getRemainingQty());

            marketOrder.reduceRemainingQty(executedQuantity);
            restingOrder.reduceRemainingQty(executedQuantity);

            System.out.println("---- Trade, price: " + bestPrice + ", qty: ----" + executedQuantity);

            // After being consumed the market order is removed from the book
            if (restingOrder.getRemainingQty() == 0) {
                priceQueue.removeFirst();
                orderBook.ordersById.remove(restingOrder.getId());
            }

            if (priceQueue.isEmpty()) {
                oppositeBook.remove(bestPrice);
            }

            orderBook.updatePeggedOrders();
        }
    }
}

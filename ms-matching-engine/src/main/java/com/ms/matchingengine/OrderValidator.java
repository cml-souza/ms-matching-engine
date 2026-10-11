package com.ms.matchingengine;

public class OrderValidator {
    OrderBook orderBook = new OrderBook();
    

    public OrderValidator(OrderBook orderBook) {
        this.orderBook = orderBook;
    }

    public boolean isValidOrder(Order order) {
        if (order == null) {
            return false;
        }

        if (order.getId() == null || order.getId().isBlank()) {
            return false;
        }

        if (orderBook.ordersById.containsKey(order.getId())) {
            return false;
        }

        if (order.getQuantity() <= 0) {
            return false;
        }

        if (order.getType() == OrderType.LIMIT) {
            if (!Double.isFinite(order.getPrice())
                    || order.getPrice() <= 0) {
                return false;
            }
        }

        return true;
    }
}

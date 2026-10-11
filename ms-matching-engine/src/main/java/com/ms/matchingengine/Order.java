package com.ms.matchingengine;

public class Order {
    private String id;
    private Side side;
    private OrderType type;
    private double price;
    private int quantity;
    private int remainingQty;

    // The use of primitive data types in this case is justified in order to maintain performance and efficiency, considering they're stored in memory and also because the values mustn't be null.
    public Order(String id, Side side, OrderType type, double price, int quantity) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty.");
        }
        if (id.length() > 5) {
            throw new IllegalArgumentException("Order ID must have 5 characters max.");
        }
        if (side == null || type == null) {
            throw new IllegalArgumentException("Side and OrderType cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (type == OrderType.LIMIT && price <= 0.0) {
            throw new IllegalArgumentException("Limit orders must have a price greater than zero.");
        }

        this.id = id;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQty = quantity;
    }


    public void reduceRemainingQty(int executedQuantity) {
        if (executedQuantity <= 0) {
            throw new IllegalArgumentException("Executed quantity must be greater than zero.");
        }
        if (executedQuantity > remainingQty) {
            throw new IllegalArgumentException("Executed quantity exceeds remaining orders quantity");
        }
        this.remainingQty -= executedQuantity;
    }

    public String getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRemainingQty() {
        return remainingQty;
    }


    // Setters for future modifications
    public void setPrice(double price) {
        if (type == OrderType.LIMIT && price <= 0.0) {
            throw new IllegalArgumentException("Limit order price must be greater than zero.");
        }
        this.price = price;
    }

    public void setQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        int executedQty = this.quantity - this.remainingQty;
        if (newQuantity < executedQty) {
            throw new IllegalArgumentException("New quantity cannot be less than already executed quantity (" + executedQty + ").");
        }
        this.quantity = newQuantity;
        this.remainingQty = newQuantity - executedQty;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", side=" + side +
                ", type=" + type +
                ", price=" + price +
                ", quantity=" + quantity +
                ", remainingQty=" + remainingQty +
                '}';
    }
}

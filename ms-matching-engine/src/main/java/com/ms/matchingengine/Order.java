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
        this.id = id;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.remainingQty = quantity;
    }


    public void reduceRemainingQty(int executedQuantity) {
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
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

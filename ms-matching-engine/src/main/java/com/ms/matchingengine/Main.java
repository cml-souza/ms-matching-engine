package com.ms.matchingengine;

public class Main {
    static void main(String[] args) {
        OrderBook book = new OrderBook();

        System.out.println("********Adicionando ordens limites********");
        Order buyOrder1 = new Order("buy1", Side.BUY, OrderType.LIMIT, 10.0, 200);
        Order sellOrder1 = new Order("sell1", Side.SELL, OrderType.LIMIT, 10.5, 100);
        Order buyOrder2 = new Order("buy2", Side.BUY, OrderType.LIMIT, 9.99, 100);


        book.addOrderToBook(buyOrder1);
        book.addOrderToBook(sellOrder1);
        book.addOrderToBook(buyOrder2);
        book.printBook();

        System.out.println("********Adicionando um PEG_BID ordem pegged de compra********");
        Order peggedBuy = new Order("peggedBuy1", Side.BUY, OrderType.PEGGED_BID, 0.0, 150);
        book.addOrderToBook(peggedBuy);
        book.updatePeggedOrders();
        book.printBook();
    }
}

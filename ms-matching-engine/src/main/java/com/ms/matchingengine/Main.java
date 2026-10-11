package com.ms.matchingengine;

public class Main {
    static void main(String[] args) {
        OrderBook orderBook = new OrderBook();

        ConsoleInterface consoleInterface = new ConsoleInterface(orderBook);
        consoleInterface.start();
    }
}

package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookPrinterTest {
    @Test
    void printsHeadersForEmptyBook() {
        String output = TestSupport.captureOutput(() -> new OrderBookPrinter(new OrderBook()).printBook());

        assertTrue(output.contains("ORDERBOOK VISUALIZATON"));
        assertTrue(output.contains("Ordens de Compra"));
        assertTrue(output.contains("Ordens de Venda"));
        assertFalse(output.contains("@"));
    }

    @Test
    void printsBuyAndSellOrdersInBookPriceOrderWithRemainingQuantities() {
        OrderBook book = new OrderBook();
        Order buy = TestSupport.order("B1", Side.BUY, OrderType.LIMIT, 10.5, 6);
        buy.reduceRemainingQty(2);
        book.addOrderToBook(TestSupport.order("B2", Side.BUY, OrderType.LIMIT, 12, 1));
        book.addOrderToBook(buy);
        book.addOrderToBook(TestSupport.order("S1", Side.SELL, OrderType.LIMIT, 15.25, 3));

        String output = TestSupport.captureOutput(() -> new OrderBookPrinter(book).printBook());

        assertTrue(output.contains("B2"));
        assertTrue(output.contains("B1"));
        assertTrue(output.contains("4 @ 10.50"));
        assertTrue(output.contains("S1"));
        assertTrue(output.contains("3 @ 15.25"));
        assertTrue(output.indexOf("B2") < output.indexOf("B1"));
    }

    @Test
    void printsOneSidedBook() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("S1", Side.SELL, OrderType.LIMIT, 8, 2));

        String output = TestSupport.captureOutput(() -> new OrderBookPrinter(book).printBook());

        assertTrue(output.contains("S1"));
        assertTrue(output.contains("2 @ 8.00"));
    }

    @Test
    void printsBuyOnlyBook() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("B1", Side.BUY, OrderType.LIMIT, 8, 2));

        String output = TestSupport.captureOutput(() -> new OrderBookPrinter(book).printBook());

        assertTrue(output.contains("B1"));
        assertTrue(output.contains("2 @ 8.00"));
    }
}

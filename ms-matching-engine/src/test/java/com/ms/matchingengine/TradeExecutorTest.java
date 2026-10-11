package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TradeExecutorTest {
    @Test
    void emptyOppositeBookRejectsMarketOrderWithoutChangingIt() {
        OrderBook book = new OrderBook();
        Order market = TestSupport.order("M", Side.BUY, OrderType.MARKET, 0, 3);

        String output = TestSupport.captureOutput(() -> new TradeExecutor(book).matchMarketOrder(market));

        assertEquals(3, market.getRemainingQty());
        assertTrue(output.contains("No liquidity available"));
    }

    @Test
    void buyMarketOrderConsumesBestAskLevelsAndRemovesFilledRestingOrders() {
        OrderBook book = new OrderBook();
        Order bestAsk = TestSupport.order("A", Side.SELL, OrderType.LIMIT, 10, 2);
        Order nextAsk = TestSupport.order("B", Side.SELL, OrderType.LIMIT, 11, 4);
        book.addOrderToBook(TestSupport.order("C", Side.SELL, OrderType.LIMIT, 11, 2));
        book.addOrderToBook(nextAsk);
        book.addOrderToBook(bestAsk);
        Order market = TestSupport.order("M", Side.BUY, OrderType.MARKET, 0, 5);

        String output = TestSupport.captureOutput(() -> new TradeExecutor(book).matchMarketOrder(market));

        assertEquals(0, market.getRemainingQty());
        assertFalse(book.getOrdersById().containsKey("A"));
        assertEquals(3, nextAsk.getRemainingQty());
        assertTrue(book.getOrdersById().containsKey("B"));
        assertFalse(book.getOrdersById().containsKey("C"));
        assertTrue(output.contains("price: 10.0"));
        assertTrue(output.contains("price: 11.0"));
        assertEquals(1, book.getSellOrders().get(11.0).size());
    }

    @Test
    void sellMarketOrderConsumesHighestBidFirstAndLeavesUnfilledRestingQuantity() {
        OrderBook book = new OrderBook();
        Order bid = TestSupport.order("B", Side.BUY, OrderType.LIMIT, 12, 5);
        book.addOrderToBook(TestSupport.order("C", Side.BUY, OrderType.LIMIT, 10, 2));
        book.addOrderToBook(bid);
        Order market = TestSupport.order("M", Side.SELL, OrderType.MARKET, 0, 2);

        TestSupport.captureOutput(() -> new TradeExecutor(book).matchMarketOrder(market));

        assertEquals(0, market.getRemainingQty());
        assertEquals(3, bid.getRemainingQty());
        assertTrue(book.getOrdersById().containsKey("B"));
        assertTrue(book.getBuyOrders().containsKey(10.0));
    }

    @Test
    void marketOrderMayFinishWithUnfilledQuantityWhenLiquidityRunsOut() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.SELL, OrderType.LIMIT, 10, 2));
        Order market = TestSupport.order("M", Side.BUY, OrderType.MARKET, 0, 5);

        TestSupport.captureOutput(() -> new TradeExecutor(book).matchMarketOrder(market));

        assertEquals(3, market.getRemainingQty());
        assertTrue(book.getSellOrders().isEmpty());
        assertFalse(book.getOrdersById().containsKey("A"));
        assertFalse(book.getOrdersById().containsKey("M"));
    }
}

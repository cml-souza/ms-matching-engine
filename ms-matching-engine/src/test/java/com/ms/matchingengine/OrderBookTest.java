package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    @Test
    void gettersExposeBookMaps() {
        OrderBook book = new OrderBook();

        assertSame(book.buyOrders, book.getBuyOrders());
        assertSame(book.sellOrders, book.getSellOrders());
        assertSame(book.ordersById, book.getOrdersById());
    }

    @Test
    void addOrderSortsPricesAndKeepsFifoAtEachPrice() {
        OrderBook book = new OrderBook();
        Order lowerBid = TestSupport.order("B1", Side.BUY, OrderType.LIMIT, 10, 1);
        Order firstAtPrice = TestSupport.order("B2", Side.BUY, OrderType.LIMIT, 12, 1);
        Order secondAtPrice = TestSupport.order("B3", Side.BUY, OrderType.LIMIT, 12, 1);
        Order ask = TestSupport.order("S1", Side.SELL, OrderType.LIMIT, 15, 1);
        book.addOrderToBook(lowerBid);
        book.addOrderToBook(firstAtPrice);
        book.addOrderToBook(secondAtPrice);
        book.addOrderToBook(ask);

        assertEquals(12.0, book.getBuyOrders().firstKey(), 0.0);
        assertEquals(10.0, book.getBuyOrders().lastKey(), 0.0);
        assertEquals(15.0, book.getSellOrders().firstKey(), 0.0);
        assertEquals(java.util.List.of(firstAtPrice, secondAtPrice), book.getBuyOrders().get(12.0));
        assertSame(ask, book.getOrdersById().get("S1"));
    }

    @Test
    void cancelRemovesOrderAndEmptyPriceLevel() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1));

        assertTrue(book.cancelOrder("A"));

        assertTrue(book.getBuyOrders().isEmpty());
        assertFalse(book.getOrdersById().containsKey("A"));
    }

    @Test
    void cancelKeepsPriceLevelWhenOtherOrdersRemainAndReturnsFalseForMissingId() {
        OrderBook book = new OrderBook();
        Order remaining = TestSupport.order("A", Side.SELL, OrderType.LIMIT, 10, 1);
        book.addOrderToBook(remaining);
        book.addOrderToBook(TestSupport.order("B", Side.SELL, OrderType.LIMIT, 10, 1));

        assertTrue(book.cancelOrder("A"));
        assertFalse(book.cancelOrder("missing"));

        assertEquals(java.util.List.of(book.getOrdersById().get("B")),
                book.getSellOrders().get(10.0));
        assertFalse(book.getOrdersById().containsKey("A"));
    }

    @Test
    void modifyReturnsFalseForMissingOrder() {
        assertFalse(new OrderBook().modifyOrder("missing", 10, 1));
    }

    @Test
    void modifyLimitChangesPriceAndQuantityAndRequeuesAtNewPrice() {
        OrderBook book = new OrderBook();
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 5);
        book.addOrderToBook(order);

        assertTrue(book.modifyOrder("A", 12, 7));

        assertFalse(book.getBuyOrders().containsKey(10.0));
        assertEquals(java.util.List.of(order), book.getBuyOrders().get(12.0));
        assertEquals(12, order.getPrice(), 0.0);
        assertEquals(7, order.getQuantity());
        assertEquals(7, order.getRemainingQty());
    }

    @Test
    void modifyAtSamePriceKeepsQueuePositionAndAccountsForPriorFills() {
        OrderBook book = new OrderBook();
        Order first = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 5);
        Order second = TestSupport.order("B", Side.BUY, OrderType.LIMIT, 10, 5);
        book.addOrderToBook(first);
        book.addOrderToBook(second);
        first.reduceRemainingQty(2);

        assertTrue(book.modifyOrder("A", 10, 6));

        assertEquals(4, first.getRemainingQty());
        assertEquals(java.util.List.of(first, second), book.getBuyOrders().get(10.0));
    }

    @Test
    void invalidLimitPriceDoesNotChangeOrderOrBook() {
        OrderBook book = new OrderBook();
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 5);
        book.addOrderToBook(order);

        assertThrows(IllegalArgumentException.class, () -> book.modifyOrder("A", 0, 8));
        assertThrows(IllegalArgumentException.class,
                () -> book.modifyOrder("A", Double.NaN, 8));

        assertEquals(10, order.getPrice(), 0.0);
        assertEquals(5, order.getQuantity());
        assertEquals(5, order.getRemainingQty());
        assertEquals(java.util.List.of(order), book.getBuyOrders().get(10.0));
        assertFalse(book.getBuyOrders().containsKey(0.0));
        assertEquals(java.util.Set.of("A"), book.getOrdersById().keySet());
    }

    @Test
    void invalidQuantityDoesNotChangePriceOrBookPosition() {
        OrderBook book = new OrderBook();
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 5);
        book.addOrderToBook(order);
        order.reduceRemainingQty(3);

        assertThrows(IllegalArgumentException.class, () -> book.modifyOrder("A", 12, 0));
        assertThrows(IllegalArgumentException.class, () -> book.modifyOrder("A", 12, 2));

        assertEquals(10, order.getPrice(), 0.0);
        assertEquals(5, order.getQuantity());
        assertEquals(2, order.getRemainingQty());
        assertEquals(java.util.List.of(order), book.getBuyOrders().get(10.0));
        assertFalse(book.getBuyOrders().containsKey(12.0));
    }

    @Test
    void modifyNonLimitChangesQuantityButNotPrice() {
        OrderBook book = new OrderBook();
        Order order = TestSupport.order("A", Side.BUY, OrderType.PEGGED_BID, 10, 5);
        book.addOrderToBook(order);

        assertTrue(book.modifyOrder("A", 99, 8));

        assertEquals(10, order.getPrice(), 0.0);
        assertEquals(8, order.getQuantity());
        assertEquals(8, order.getRemainingQty());
    }

    @Test
    void peggedBidTracksBestNonPeggedBidAndRepricesAfterCancellation() {
        OrderBook book = new OrderBook();
        Order pegged = TestSupport.order("P", Side.BUY, OrderType.PEGGED_BID, 1, 1);
        book.addOrderToBook(pegged);
        Order bid10 = TestSupport.order("B1", Side.BUY, OrderType.LIMIT, 10, 1);
        book.addOrderToBook(bid10);
        Order bid12 = TestSupport.order("B2", Side.BUY, OrderType.LIMIT, 12, 1);
        book.addOrderToBook(bid12);

        assertEquals(12, pegged.getPrice(), 0.0);
        assertTrue(book.cancelOrder("B2"));

        assertEquals(10, pegged.getPrice(), 0.0);
        assertSame(pegged, book.getBuyOrders().get(10.0).getLast());
    }

    @Test
    void peggedOrderKeepsLastPriceWhenItsReferenceOrderIsRemoved() {
        OrderBook book = new OrderBook();
        Order pegged = TestSupport.order("P", Side.BUY, OrderType.PEGGED_BID, 1, 1);
        Order reference = TestSupport.order("B1", Side.BUY, OrderType.LIMIT, 10, 1);
        book.addOrderToBook(pegged);
        book.addOrderToBook(reference);

        assertEquals(10, pegged.getPrice(), 0.0);
        assertTrue(book.cancelOrder("B1"));

        assertEquals(10, pegged.getPrice(), 0.0);
        assertEquals(java.util.List.of(pegged), book.getBuyOrders().get(10.0));
    }

    @Test
    void peggedOfferTracksBestNonPeggedOffer() {
        OrderBook book = new OrderBook();
        Order pegged = TestSupport.order("P", Side.SELL, OrderType.PEGGED_OFFER, 1, 1);
        book.addOrderToBook(pegged);
        book.addOrderToBook(TestSupport.order("S1", Side.SELL, OrderType.LIMIT, 15, 1));
        book.addOrderToBook(TestSupport.order("S2", Side.SELL, OrderType.LIMIT, 13, 1));

        assertEquals(13, pegged.getPrice(), 0.0);
        assertEquals(13.0, book.getSellOrders().firstKey(), 0.0);
    }

    @Test
    void peggedOrdersDoNotUseOtherPeggedOrdersAsTheirReferencePrice() {
        OrderBook book = new OrderBook();
        Order peggedBid = TestSupport.order("PB", Side.BUY, OrderType.PEGGED_BID, 1, 1);
        Order peggedOffer = TestSupport.order("PO", Side.SELL, OrderType.PEGGED_OFFER, 1, 1);
        book.addOrderToBook(peggedBid);
        book.addOrderToBook(peggedOffer);
        book.updatePeggedOrders();

        assertEquals(1, peggedBid.getPrice(), 0.0);
        assertEquals(1, peggedOffer.getPrice(), 0.0);
    }

    @Test
    void publicPriceQueuesContainLinkedLists() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1));

        assertInstanceOf(LinkedList.class, book.getBuyOrders().get(10.0));
    }
}

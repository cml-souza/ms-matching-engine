package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidatorTest {
    @Test
    void rejectsNullOrder() {
        assertFalse(new OrderValidator(new OrderBook()).isValidOrder(null));
    }

    @Test
    void acceptsValidLimitMarketAndPeggedOrders() {
        OrderValidator validator = new OrderValidator(new OrderBook());

        assertTrue(validator.isValidOrder(TestSupport.order("L", Side.BUY, OrderType.LIMIT, 10, 1)));
        assertTrue(validator.isValidOrder(TestSupport.order("M", Side.SELL, OrderType.MARKET, 0, 1)));
        assertTrue(validator.isValidOrder(TestSupport.order("P", Side.BUY, OrderType.PEGGED_BID, 0, 1)));
        assertTrue(validator.isValidOrder(TestSupport.order("O", Side.SELL, OrderType.PEGGED_OFFER, 0, 1)));
    }

    @Test
    void rejectsDuplicateId() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1));

        assertFalse(new OrderValidator(book).isValidOrder(
                TestSupport.order("A", Side.SELL, OrderType.LIMIT, 11, 1)));
    }

    @Test
    void rejectsNonFiniteOrNonpositiveLimitPrice() {
        OrderValidator validator = new OrderValidator(new OrderBook());

        assertFalse(validator.isValidOrder(TestSupport.order("N", Side.BUY, OrderType.LIMIT, Double.NaN, 1)));
        assertFalse(validator.isValidOrder(TestSupport.order("I", Side.BUY, OrderType.LIMIT, Double.POSITIVE_INFINITY, 1)));
    }
}

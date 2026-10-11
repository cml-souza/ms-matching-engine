package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    void constructorInitializesFieldsAndRemainingQuantity() {
        Order order = TestSupport.order("A1", Side.BUY, OrderType.LIMIT, 12.5, 8);

        assertAll(
                () -> assertEquals("A1", order.getId()),
                () -> assertEquals(Side.BUY, order.getSide()),
                () -> assertEquals(OrderType.LIMIT, order.getType()),
                () -> assertEquals(12.5, order.getPrice(), 0.0),
                () -> assertEquals(8, order.getQuantity()),
                () -> assertEquals(8, order.getRemainingQty()));
    }

    @Test
    void constructorRejectsInvalidIds() {
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order(null, Side.BUY, OrderType.LIMIT, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("  ", Side.BUY, OrderType.LIMIT, 1, 1));
        assertDoesNotThrow(
                () -> TestSupport.order("ID_100", Side.BUY, OrderType.LIMIT, 1, 1));
    }

    @Test
    void constructorRejectsNullSideOrTypeAndNonpositiveQuantity() {
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", null, OrderType.LIMIT, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", Side.BUY, null, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", Side.BUY, OrderType.LIMIT, 1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", Side.BUY, OrderType.LIMIT, 1, -1));
    }

    @Test
    void limitOrderRequiresPositivePrice() {
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", Side.BUY, OrderType.LIMIT, 0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> TestSupport.order("A", Side.BUY, OrderType.LIMIT, -1, 1));
    }

    @Test
    void nonLimitOrderMayBeCreatedWithoutPositivePrice() {
        assertDoesNotThrow(() -> TestSupport.order("A", Side.BUY, OrderType.MARKET, 0, 1));
        assertDoesNotThrow(() -> TestSupport.order("B", Side.SELL, OrderType.PEGGED_OFFER, -1, 1));
    }

    @Test
    void reduceRemainingQuantityTracksExecutions() {
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 7);

        order.reduceRemainingQty(3);

        assertEquals(4, order.getRemainingQty());
    }

    @Test
    void reduceRemainingQuantityRejectsInvalidExecutionAmounts() {
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 3);

        assertThrows(IllegalArgumentException.class, () -> order.reduceRemainingQty(0));
        assertThrows(IllegalArgumentException.class, () -> order.reduceRemainingQty(-1));
        assertThrows(IllegalArgumentException.class, () -> order.reduceRemainingQty(4));
    }

    @Test
    void setPriceUpdatesValidLimitAndAllowsNonLimitPrice() {
        Order limit = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1);
        Order market = TestSupport.order("B", Side.BUY, OrderType.MARKET, 0, 1);

        limit.setPrice(12.5);
        market.setPrice(-2);

        assertEquals(12.5, limit.getPrice(), 0.0);
        assertEquals(-2, market.getPrice(), 0.0);
    }

    @Test
    void setPriceRejectsNonpositiveLimitPrice() {
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1);

        assertThrows(IllegalArgumentException.class, () -> order.setPrice(0));
        assertThrows(IllegalArgumentException.class, () -> order.setPrice(-1));
        assertEquals(10, order.getPrice(), 0.0);
    }

    @Test
    void setQuantityPreservesAlreadyExecutedQuantity() {
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 10);
        order.reduceRemainingQty(4);

        order.setQuantity(9);

        assertEquals(9, order.getQuantity());
        assertEquals(5, order.getRemainingQty());
    }

    @Test
    void setQuantityRejectsInvalidValuesAndQuantityBelowFilledAmount() {
        Order order = TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 5);
        order.reduceRemainingQty(3);

        assertThrows(IllegalArgumentException.class, () -> order.setQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> order.setQuantity(-1));
        assertThrows(IllegalArgumentException.class, () -> order.setQuantity(2));
    }

    @Test
    void toStringIncludesOrderState() {
        Order order = TestSupport.order("A", Side.SELL, OrderType.LIMIT, 2.5, 4);
        order.reduceRemainingQty(1);

        assertEquals("Order{id='A', side=SELL, type=LIMIT, price=2.5, quantity=4, remainingQty=3}",
                order.toString());
    }
}

package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class OrderTypeTest {
    @Test
    void exposesSupportedOrderTypes() {
        assertArrayEquals(new OrderType[]{
                OrderType.LIMIT,
                OrderType.MARKET,
                OrderType.PEGGED_BID,
                OrderType.PEGGED_OFFER
        }, OrderType.values());
    }
}

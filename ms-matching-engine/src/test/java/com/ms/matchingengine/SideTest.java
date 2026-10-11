package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class SideTest {
    @Test
    void exposesBuyAndSellSides() {
        assertArrayEquals(new Side[]{Side.BUY, Side.SELL}, Side.values());
    }
}

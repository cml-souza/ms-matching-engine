package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderIdGeneratorTest {
    @Test
    void generatesSequentialIdsStartingAtOne() {
        OrderIdGenerator generator = new OrderIdGenerator();

        assertEquals("ID_1", generator.generateId());
        assertEquals("ID_2", generator.generateId());
        assertEquals("ID_3", generator.generateId());
    }

    @Test
    void continuesGeneratingIdsPastTwoDigitsAndOrderAcceptsThem() {
        OrderIdGenerator generator = new OrderIdGenerator();
        for (int i = 0; i < 99; i++) {
            generator.generateId();
        }

        String id = generator.generateId();

        assertEquals("ID_100", id);
        assertEquals(id, TestSupport.order(id, Side.BUY, OrderType.LIMIT, 10, 1).getId());
    }
}

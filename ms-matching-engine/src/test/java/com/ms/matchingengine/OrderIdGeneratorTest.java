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
}

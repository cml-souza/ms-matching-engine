package com.ms.matchingengine;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

final class TestSupport {
    private TestSupport() {
    }

    static String captureOutput(Runnable action) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capturedOut = new PrintStream(output, true, StandardCharsets.UTF_8)) {
            System.setOut(capturedOut);
            action.run();
        } finally {
            System.setOut(originalOut);
        }
        return output.toString(StandardCharsets.UTF_8);
    }

    static String runConsole(OrderBook orderBook, String input) {
        InputStream originalIn = System.in;
        try {
            System.setIn(new java.io.ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            return captureOutput(() -> new ConsoleInterface(orderBook).start());
        } finally {
            System.setIn(originalIn);
        }
    }

    static Order order(String id, Side side, OrderType type, double price, int quantity) {
        return new Order(id, side, type, price, quantity);
    }
}

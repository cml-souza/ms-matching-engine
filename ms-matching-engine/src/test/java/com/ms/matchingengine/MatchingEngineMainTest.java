package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingEngineMainTest {
    @Test
    void mainStartsConsoleAndAcceptsExit() {
        InputStream originalIn = System.in;
        try {
            System.setIn(new ByteArrayInputStream("5\n".getBytes(StandardCharsets.UTF_8)));
            String output = TestSupport.captureOutput(() -> Main.main(new String[0]));

            assertTrue(output.contains("MATCHING ENGINE"));
            assertTrue(output.contains("Closing the Matching Engine"));
        } finally {
            System.setIn(originalIn);
        }
    }
}

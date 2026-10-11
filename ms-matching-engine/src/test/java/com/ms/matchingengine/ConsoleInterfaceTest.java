package com.ms.matchingengine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleInterfaceTest {
    @Test
    void exitsFromMenu() {
        String output = TestSupport.runConsole(new OrderBook(), "5\n");

        assertTrue(output.contains("MATCHING ENGINE"));
        assertTrue(output.contains("Closing the Matching Engine"));
    }

    @Test
    void reportsInvalidMenuInputAndUnknownOption() {
        String output = TestSupport.runConsole(new OrderBook(), "text\n9\n5\n");

        assertTrue(output.contains("Please enter a valid value"));
        assertTrue(output.contains("Only numbers from the list are valid"));
    }

    @Test
    void addsLimitOrderAndDisplaysBook() {
        OrderBook book = new OrderBook();
        String input = "1\n1\n1\n10.5\n4\n\n2\n5\n";

        String output = TestSupport.runConsole(book, input);

        assertTrue(book.getOrdersById().containsKey("ID_1"));
        assertEquals(10.5, book.getOrdersById().get("ID_1").getPrice(), 0.0);
        assertTrue(output.contains("Order submitted successfully."));
        assertTrue(output.contains("ID_1"));
    }

    @Test
    void rejectsMarketOrderWhenOppositeBookIsEmpty() {
        OrderBook book = new OrderBook();
        String output = TestSupport.runConsole(book, "1\n1\n2\n5\n");

        assertTrue(output.contains("Cannot submit MARKET order"));
        assertTrue(book.getOrdersById().isEmpty());
    }

    @Test
    void rejectsInvalidSideAndOrderTypeSelections() {
        String invalidSideOutput = TestSupport.runConsole(new OrderBook(), "1\n9\n5\n");
        String invalidTypeOutput = TestSupport.runConsole(new OrderBook(), "1\n1\n9\n5\n");

        assertTrue(invalidSideOutput.contains("Only numbers from list are valid"));
        assertTrue(invalidTypeOutput.contains("Please enter a valid value."));
    }

    @Test
    void addsPeggedBidWithoutAnExplicitPrice() {
        OrderBook book = new OrderBook();
        String input = "1\n1\n3\n2\n\n5\n";

        String output = TestSupport.runConsole(book, input);

        assertTrue(output.contains("Order submitted successfully."));
        assertTrue(book.getOrdersById().containsKey("ID_1"));
        assertEquals(OrderType.PEGGED_BID, book.getOrdersById().get("ID_1").getType());
    }

    @Test
    void retriesInvalidLimitPriceInput() {
        OrderBook book = new OrderBook();
        String input = "1\n1\n1\ninvalid\n10\n2\n\n5\n";
        String output = TestSupport.runConsole(book, input);

        assertTrue(output.contains("Please enter a valid value (Ex: 10.5 or 10,5)."));
        assertEquals(10.0, book.getOrdersById().get("ID_1").getPrice(), 0.0);
        assertEquals(2, book.getOrdersById().get("ID_1").getQuantity());
    }

    @Test
    void matchesMarketOrderAgainstExistingLiquidity() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("ASK", Side.SELL, OrderType.LIMIT, 10, 3));
        String input = "1\n1\n2\n2\n\n5\n";

        String output = TestSupport.runConsole(book, input);

        assertEquals(1, book.getOrdersById().get("ASK").getRemainingQty());
        assertTrue(output.contains("price: 10.0"));
    }

    @Test
    void cancelsExistingOrderThroughMenu() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 1));
        String output = TestSupport.runConsole(book, "3\nA\n5\n");

        assertTrue(output.contains("Order cancelled: A"));
        assertFalse(book.getOrdersById().containsKey("A"));
    }

    @Test
    void reportsMissingOrderWhenCancelling() {
        String output = TestSupport.runConsole(new OrderBook(), "3\nmissing\n5\n");

        assertTrue(output.contains("Order not found: missing"));
    }

    @Test
    void reportsMissingOrderWhenModifying() {
        String output = TestSupport.runConsole(new OrderBook(), "4\nmissing\n5\n");

        assertTrue(output.contains("Order not found."));
    }

    @Test
    void modifiesLimitOrderThroughMenu() {
        OrderBook book = new OrderBook();
        book.addOrderToBook(TestSupport.order("A", Side.BUY, OrderType.LIMIT, 10, 3));
        String output = TestSupport.runConsole(book, "4\nA\n11\n5\n5\n");

        assertEquals(11, book.getOrdersById().get("A").getPrice(), 0.0);
        assertEquals(5, book.getOrdersById().get("A").getQuantity());
        assertTrue(output.contains("Order modified: A"));
    }

    @Test
    void modifiesPeggedOrderQuantityWithoutChangingItsPrice() {
        OrderBook book = new OrderBook();
        Order pegged = TestSupport.order("P", Side.BUY, OrderType.PEGGED_BID, 10, 3);
        book.addOrderToBook(pegged);

        String output = TestSupport.runConsole(book, "4\nP\n5\n5\n");

        assertEquals(10, pegged.getPrice(), 0.0);
        assertEquals(5, pegged.getQuantity());
        assertTrue(output.contains("Order modified: P"));
    }
}

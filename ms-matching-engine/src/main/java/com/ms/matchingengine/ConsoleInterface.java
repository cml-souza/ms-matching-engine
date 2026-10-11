package com.ms.matchingengine;

import java.util.Scanner;

public class ConsoleInterface {
    private OrderBook orderBook;
    private OrderBookPrinter orderBookPrinter;
    private OrderValidator orderValidator;
    private TradeExecutor tradeExecutor;
    private OrderIdGenerator orderIdGenerator;
    private Scanner scanner;

    public ConsoleInterface(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.scanner = new Scanner(System.in);
        this.orderBookPrinter = new OrderBookPrinter(orderBook);
        this.orderValidator = new OrderValidator(orderBook);
        this.tradeExecutor = new TradeExecutor(orderBook);
        this.orderIdGenerator = new OrderIdGenerator();
    }

    public void start() {
        boolean running = true;
        System.out.println("=== MATCHING ENGINE - INTERACTIVE CONSOLE ===");

        while (running) {
            printMenu();
            int input = readIntInput();

            switch (input) {
                case 1:
                    handleAddOrder();
                    break;
                case 2:
                    orderBookPrinter.printBook();
                    break;
                case 3:
                    handleCancelOrder();
                    break;
                case 4:
                    handleModifyOrder();
                    break;
                case 5:
                    running = false;
                    System.out.println("----- Closing the Matching Engine -----");
                    break;
                default:
                    System.out.println("Unknown option. Please try again.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\nChoose an option:");
        System.out.println("1. Add Order (Limit / Market / Pegged)");
        System.out.println("2. View Order Book");
        System.out.println("3. Cancel Order by ID");
        System.out.println("4. Modify Order");
        System.out.println("5. Exit");
        System.out.print("Option: ");
    }

    private int readIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.println("Error: Please enter a valid value (Ex: 1, 2, 3).");
            scanner.next();
            System.out.println("Try Again: ");
        }
        int value = scanner.nextInt();
        scanner.nextLine();
        return value;
    }

    private double readDoubleInput() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid value (Ex: 10.5 or 10,5).");
                System.out.println("Try Again: ");
            }
        }
    }

    private void handleAddOrder() {
        String id = orderIdGenerator.generateId();
        System.out.println("Order ID: " + id);

        if (id.isEmpty() || id.length() > 5) {
            System.out.println("Error: The ID must be between 1 and 5 characters long.");
            return;
        }

        System.out.println("Choose the Side:");
        System.out.println("1. BUY");
        System.out.println("2. SELL");
        System.out.print("Option (Side): ");
        int sideOption = readIntInput();
        Side side;
        switch (sideOption) {
            case 1:
                side = Side.BUY;
                break;
            case 2:
                side = Side.SELL;
                break;
            default:
                System.out.println("Please enter a valid side.");
                return;
        }

        System.out.println("Choose the Order Type:");
        System.out.println("1. LIMIT");
        System.out.println("2. MARKET");
        System.out.println("3. PEGGED");
        System.out.print("Option (Type): ");
        int typeOption = readIntInput();

        OrderType type;
        switch (typeOption) {
            case 1:
                type = OrderType.LIMIT;
                break;
            case 2:
                type = OrderType.MARKET;
                boolean hasLiquidity = (side == Side.BUY) ? !orderBook.getSellOrders().isEmpty() : !orderBook.getBuyOrders().isEmpty();
                if (!hasLiquidity) {
                    System.out.println("Rejection: Cannot submit MARKET order. Order book is empty on the opposite side!");
                    return;
                }
                break;
            case 3:
                type = (side == Side.BUY) ? OrderType.PEGGED_BID : OrderType.PEGGED_OFFER;
                break;
            default:
                System.out.println("Please enter a valid value.");
                return;
        }

        double price = readDoubleInput();
        if (type == OrderType.LIMIT) {
            System.out.println("Enter price: ");
            String priceInput = scanner.nextLine();
            try {
                price = Double.parseDouble(priceInput.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Please enter a valid number.");
                return;
            }
        }

        System.out.print("Enter quantity (shares): ");
        int quantity = readIntInput();
        scanner.nextLine();

        Order order = new Order(id, side, type, price, quantity);

            if (orderValidator.isValidOrder(order)) {
                if (type == OrderType.MARKET) {
                    tradeExecutor.matchMarketOrder(order);
                } else {
                    orderBook.addOrderToBook(order);
                    System.out.println("Order submitted successfully.");
                }
            } else {
                System.out.println("Order rejected. Invalid order data.");
            }

    }

    private void handleCancelOrder() {
        System.out.println("Enter the ID of the order you want to cancel:");
        String orderlId = scanner.nextLine();
        orderBook.cancelOrder(orderlId);
    }

    private void handleModifyOrder() {
        System.out.println("Enter the ID of the order to be modified:");
        String orderId = scanner.nextLine();

        Order existingOrder = orderBook.getOrdersById().get(orderId);
        if (existingOrder == null) {
            System.out.println("Order not found.");
            return;
        }

        double newPrice = 0.0;
        if (existingOrder.getType() == OrderType.LIMIT) {
            System.out.println("Enter the new price: ");
            String priceInput = scanner.nextLine();
            try {
                newPrice = Double.parseDouble(priceInput.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Please enter a valid number.");
                return;
            }
        }

        System.out.println("Enter the new quantity: ");
        int newQuantity = scanner.nextInt();
        scanner.nextLine();

        orderBook.modifyOrder(orderId, newPrice, newQuantity);
    }
}

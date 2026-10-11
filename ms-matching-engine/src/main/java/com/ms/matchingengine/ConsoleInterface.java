package com.ms.matchingengine;

import java.util.Scanner;

public class ConsoleInterface {
    private OrderBook orderBook;
    private Scanner scanner;

    public ConsoleInterface(OrderBook orderBook) {
        this.orderBook = orderBook;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        System.out.println("=== MATCHING ENGINE - INTERACTIVE CONSOLE ===");

        while (running) {
            printMenu();
            int input = readInput();

            switch (input) {
                case 1:
                    handleAddOrder();
                    break;
                case 2:
                    System.out.println("\n--- ORDERBOOK VISUALIZATON ---");
                    orderBook.printBook();
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

    private int readInput() {
        if (scanner.hasNextInt()) {
            int input = scanner.nextInt();
            scanner.nextLine();
            return input;
        } else {
            scanner.nextLine();
            return -1;
        }
    }

    private void handleAddOrder() {
        System.out.println("Enter order ID: ");
        String id = scanner.nextLine();

        System.out.println("Choose the Side:");
        System.out.println("1. BUY");
        System.out.println("2. SELL");
        System.out.print("Option (Side): ");
        int sideOption = readInput();
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
        int typeOption = readInput();

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

        double price = 0.0;
        if (type == OrderType.LIMIT) {
            System.out.println("Enter price: ");
            String priceInput = scanner.nextLine();
            try {
                price = Double.parseDouble(priceInput.replace(',', '.')); // Aceita tanto vírgula quanto ponto
            } catch (NumberFormatException e) {
                System.out.println("Invalid price format. Please enter a valid number.");
                return;
            }
        }

        System.out.print("Enter quantity (shares): ");
        int quantity = scanner.nextInt();
        scanner.nextLine();

        Order order = new Order(id, side, type, price, quantity);

            if (orderBook.isValidOrder(order)) {
                if (type == OrderType.MARKET) {
                    orderBook.matchMarketOrder(order);
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
            newPrice = scanner.nextDouble();
            scanner.nextLine();
        }

        System.out.println("Enter the new quantity: ");
        int newQuantity = scanner.nextInt();
        scanner.nextLine();

        orderBook.modifyOrder(orderId, newPrice, newQuantity);
    }
}

package com.dds.server;

import com.dds.shared.AdminReport;
import com.dds.shared.Branch;
import com.dds.shared.BranchSales;
import com.dds.shared.CustomerOrder;
import com.dds.shared.DrinkBrand;
import com.dds.shared.OrderItem;
import com.dds.shared.OrderRecord;
import com.dds.shared.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BusinessState {
    private final DatabaseManager databaseManager;

    public BusinessState() {
        this.databaseManager = new DatabaseManager();
    }

    public synchronized Response placeOrder(CustomerOrder order) {
        if (order == null) {
            return new Response(false, "Order is required.", null);
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            return new Response(false, "Customer name is required.", null);
        }
        if (order.getBranch() == null) {
            return new Response(false, "Branch is required.", null);
        }
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return new Response(false, "At least one drink item is required.", null);
        }

        try (Connection connection = databaseManager.openConnection()) {
            connection.setAutoCommit(false);

            try {
                int customerId = findOrCreateCustomer(connection, order.getCustomerName().trim());
                int branchId = findBranchId(connection, order.getBranch());

                List<PendingOrderItem> pendingItems = new ArrayList<>();
                List<String> newAlerts = new ArrayList<>();
                double totalAmount = 0.0;

                for (OrderItem item : order.getItems()) {
                    if (item.getDrinkBrand() == null) {
                        connection.rollback();
                        return new Response(false, "Drink selection is required.", null);
                    }
                    if (item.getQuantity() <= 0) {
                        connection.rollback();
                        return new Response(false, "Quantity must be greater than zero.", null);
                    }

                    DrinkDetails drinkDetails = findDrinkDetails(connection, item.getDrinkBrand());
                    StockDetails stockDetails = lockStock(connection, branchId, drinkDetails.drinkId());
                    if (stockDetails.quantityAvailable() < item.getQuantity()) {
                        connection.rollback();
                        return new Response(
                                false,
                                "Not enough stock for " + item.getDrinkBrand().getDisplayName()
                                        + " at " + order.getBranch().getDisplayName() + ".",
                                null
                        );
                    }

                    int remainingQuantity = stockDetails.quantityAvailable() - item.getQuantity();
                    double lineTotal = drinkDetails.unitPrice() * item.getQuantity();
                    totalAmount += lineTotal;

                    pendingItems.add(new PendingOrderItem(
                            item.getDrinkBrand(),
                            drinkDetails.drinkId(),
                            item.getQuantity(),
                            drinkDetails.unitPrice(),
                            lineTotal,
                            remainingQuantity
                    ));

                    if (remainingQuantity <= stockDetails.minimumThreshold()) {
                        String alertMessage = "Low stock at " + order.getBranch().getDisplayName()
                                + " for " + item.getDrinkBrand().getDisplayName()
                                + ". Remaining bottles: " + remainingQuantity
                                + ", threshold: " + stockDetails.minimumThreshold();
                        insertAlert(connection, branchId, drinkDetails.drinkId(), remainingQuantity,
                                stockDetails.minimumThreshold(), alertMessage);
                        newAlerts.add(alertMessage);
                    }
                }

                OrderRecord record = insertOrder(connection, customerId, branchId, order, pendingItems, totalAmount);
                connection.commit();

                StringBuilder message = new StringBuilder();
                message.append("Order processed successfully. Order ID: ")
                        .append(record.getOrderId())
                        .append(", Total: KES ")
                        .append(String.format("%.2f", totalAmount));
                if (!newAlerts.isEmpty()) {
                    message.append(" | ALERT: ").append(String.join(" | ", newAlerts));
                }
                return new Response(true, message.toString(), record);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception exception) {
            return new Response(false, "Unable to process order: " + exception.getMessage(), null);
        }
    }

    public synchronized Map<DrinkBrand, Double> getMenu() {
        Map<DrinkBrand, Double> menu = new EnumMap<>(DrinkBrand.class);
        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            menu.put(drinkBrand, drinkBrand.getUnitPrice());
        }
        return menu;
    }

    public synchronized AdminReport buildAdminReport() {
        try (Connection connection = databaseManager.openConnection()) {
            List<OrderRecord> orders = loadOrders(connection);
            List<BranchSales> branchSales = loadBranchSales(connection);
            double totalBusinessSales = branchSales.stream().mapToDouble(BranchSales::getSalesAmount).sum();
            Map<Branch, Map<DrinkBrand, Integer>> stockSnapshot = loadStockSnapshot(connection);
            List<String> alerts = loadAlerts(connection);

            return new AdminReport(orders, branchSales, totalBusinessSales, stockSnapshot, alerts);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to build admin report: " + exception.getMessage(), exception);
        }
    }

    private int findOrCreateCustomer(Connection connection, String customerName) throws SQLException {
        String findSql = "SELECT customer_id FROM customers WHERE customer_name = ? ORDER BY customer_id DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(findSql)) {
            statement.setString(1, customerName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("customer_id");
                }
            }
        }

        String insertSql = "INSERT INTO customers (customer_name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customerName);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Customer record could not be created.");
    }

    private int findBranchId(Connection connection, Branch branch) throws SQLException {
        String sql = "SELECT branch_id FROM branches WHERE branch_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, branch.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("branch_id");
                }
            }
        }
        throw new SQLException("Branch " + branch.name() + " was not found in the database.");
    }

    private DrinkDetails findDrinkDetails(Connection connection, DrinkBrand drinkBrand) throws SQLException {
        String sql = "SELECT drink_id, unit_price FROM drinks WHERE drink_code = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, drinkBrand.name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new DrinkDetails(resultSet.getInt("drink_id"), resultSet.getDouble("unit_price"));
                }
            }
        }
        throw new SQLException("Drink " + drinkBrand.name() + " was not found in the database.");
    }

    private StockDetails lockStock(Connection connection, int branchId, int drinkId) throws SQLException {
        String sql = "SELECT quantity_available, minimum_threshold FROM stock_levels WHERE branch_id = ? AND drink_id = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, drinkId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new StockDetails(
                            resultSet.getInt("quantity_available"),
                            resultSet.getInt("minimum_threshold")
                    );
                }
            }
        }
        throw new SQLException("Stock record is missing for branch " + branchId + " and drink " + drinkId + ".");
    }

    private void insertAlert(Connection connection, int branchId, int drinkId, int remaining, int threshold,
                             String alertMessage) throws SQLException {
        String sql = "INSERT INTO stock_alerts (branch_id, drink_id, quantity_remaining, minimum_threshold, alert_message) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, branchId);
            statement.setInt(2, drinkId);
            statement.setInt(3, remaining);
            statement.setInt(4, threshold);
            statement.setString(5, alertMessage);
            statement.executeUpdate();
        }
    }

    private OrderRecord insertOrder(Connection connection, int customerId, int branchId, CustomerOrder order,
                                    List<PendingOrderItem> pendingItems, double totalAmount) throws SQLException {
        String insertOrderSql = "INSERT INTO orders (customer_id, branch_id, total_amount) VALUES (?, ?, ?)";
        long orderId;
        LocalDateTime orderTime = LocalDateTime.now();

        try (PreparedStatement statement = connection.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, customerId);
            statement.setInt(2, branchId);
            statement.setDouble(3, totalAmount);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Order record could not be created.");
                }
                orderId = keys.getLong(1);
            }
        }

        String insertItemSql = "INSERT INTO order_items (order_id, drink_id, quantity, unit_price, line_total) VALUES (?, ?, ?, ?, ?)";
        String updateStockSql = "UPDATE stock_levels SET quantity_available = ? WHERE branch_id = ? AND drink_id = ?";

        try (PreparedStatement itemStatement = connection.prepareStatement(insertItemSql);
             PreparedStatement stockStatement = connection.prepareStatement(updateStockSql)) {
            for (PendingOrderItem item : pendingItems) {
                itemStatement.setLong(1, orderId);
                itemStatement.setInt(2, item.drinkId());
                itemStatement.setInt(3, item.quantity());
                itemStatement.setDouble(4, item.unitPrice());
                itemStatement.setDouble(5, item.lineTotal());
                itemStatement.addBatch();

                stockStatement.setInt(1, item.remainingQuantity());
                stockStatement.setInt(2, branchId);
                stockStatement.setInt(3, item.drinkId());
                stockStatement.addBatch();
            }

            itemStatement.executeBatch();
            stockStatement.executeBatch();
        }

        List<OrderItem> recordItems = new ArrayList<>();
        for (PendingOrderItem item : pendingItems) {
            recordItems.add(new OrderItem(item.drinkBrand(), item.quantity()));
        }
        return new OrderRecord(orderId, order.getCustomerName(), order.getBranch(), recordItems, totalAmount, orderTime);
    }

    private List<OrderRecord> loadOrders(Connection connection) throws SQLException {
        String sql = "SELECT o.order_id, c.customer_name, b.branch_code, o.total_amount, o.order_date, d.drink_code, oi.quantity "
                + "FROM orders o "
                + "JOIN customers c ON c.customer_id = o.customer_id "
                + "JOIN branches b ON b.branch_id = o.branch_id "
                + "LEFT JOIN order_items oi ON oi.order_id = o.order_id "
                + "LEFT JOIN drinks d ON d.drink_id = oi.drink_id "
                + "ORDER BY o.order_id ASC, oi.order_item_id ASC";

        Map<Long, OrderAccumulator> orders = new LinkedHashMap<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                long orderId = resultSet.getLong("order_id");
                OrderAccumulator accumulator = orders.get(orderId);
                if (accumulator == null) {
                    Timestamp timestamp = resultSet.getTimestamp("order_date");
                    accumulator = new OrderAccumulator(
                            orderId,
                            resultSet.getString("customer_name"),
                            Branch.valueOf(resultSet.getString("branch_code")),
                            resultSet.getDouble("total_amount"),
                            timestamp == null ? LocalDateTime.now() : timestamp.toLocalDateTime()
                    );
                    orders.put(orderId, accumulator);
                }

                String drinkCode = resultSet.getString("drink_code");
                if (drinkCode != null) {
                    accumulator.items.add(new OrderItem(
                            DrinkBrand.valueOf(drinkCode),
                            resultSet.getInt("quantity")
                    ));
                }
            }
        }

        List<OrderRecord> records = new ArrayList<>();
        for (OrderAccumulator accumulator : orders.values()) {
            records.add(new OrderRecord(
                    accumulator.orderId,
                    accumulator.customerName,
                    accumulator.branch,
                    accumulator.items,
                    accumulator.totalAmount,
                    accumulator.orderTime
            ));
        }
        return records;
    }

    private List<BranchSales> loadBranchSales(Connection connection) throws SQLException {
        String sql = "SELECT b.branch_code, COALESCE(SUM(o.total_amount), 0.00) AS total_sales "
                + "FROM branches b LEFT JOIN orders o ON o.branch_id = b.branch_id "
                + "GROUP BY b.branch_code ORDER BY b.branch_id";
        List<BranchSales> sales = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                sales.add(new BranchSales(
                        Branch.valueOf(resultSet.getString("branch_code")),
                        resultSet.getDouble("total_sales")
                ));
            }
        }
        return sales;
    }

    private Map<Branch, Map<DrinkBrand, Integer>> loadStockSnapshot(Connection connection) throws SQLException {
        String sql = "SELECT b.branch_code, d.drink_code, s.quantity_available "
                + "FROM stock_levels s "
                + "JOIN branches b ON b.branch_id = s.branch_id "
                + "JOIN drinks d ON d.drink_id = s.drink_id "
                + "ORDER BY b.branch_id, d.drink_id";

        Map<Branch, Map<DrinkBrand, Integer>> snapshot = new EnumMap<>(Branch.class);
        for (Branch branch : Branch.values()) {
            snapshot.put(branch, new EnumMap<>(DrinkBrand.class));
        }

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Branch branch = Branch.valueOf(resultSet.getString("branch_code"));
                DrinkBrand drinkBrand = DrinkBrand.valueOf(resultSet.getString("drink_code"));
                snapshot.get(branch).put(drinkBrand, resultSet.getInt("quantity_available"));
            }
        }
        return snapshot;
    }

    private List<String> loadAlerts(Connection connection) throws SQLException {
        String sql = "SELECT alert_message FROM stock_alerts ORDER BY created_at DESC, alert_id DESC";
        List<String> alerts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                alerts.add(resultSet.getString("alert_message"));
            }
        }
        return alerts;
    }

    private record DrinkDetails(int drinkId, double unitPrice) {
    }

    private record StockDetails(int quantityAvailable, int minimumThreshold) {
    }

    private record PendingOrderItem(DrinkBrand drinkBrand, int drinkId, int quantity, double unitPrice,
                                    double lineTotal, int remainingQuantity) {
    }

    private static final class OrderAccumulator {
        private final long orderId;
        private final String customerName;
        private final Branch branch;
        private final double totalAmount;
        private final LocalDateTime orderTime;
        private final List<OrderItem> items = new ArrayList<>();

        private OrderAccumulator(long orderId, String customerName, Branch branch, double totalAmount,
                                 LocalDateTime orderTime) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.branch = branch;
            this.totalAmount = totalAmount;
            this.orderTime = orderTime;
        }
    }
}

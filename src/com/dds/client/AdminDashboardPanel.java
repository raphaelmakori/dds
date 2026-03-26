package com.dds.client;

import com.dds.shared.AdminReport;
import com.dds.shared.Branch;
import com.dds.shared.BranchSales;
import com.dds.shared.DrinkBrand;
import com.dds.shared.OrderItem;
import com.dds.shared.OrderRecord;
import com.dds.shared.Request;
import com.dds.shared.RequestType;
import com.dds.shared.Response;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class AdminDashboardPanel extends JPanel {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("5050");
    private final JLabel statusLabel = new JLabel("Disconnected");
    private final JLabel totalSalesLabel = new JLabel("KES 0.00");
    private final JLabel countsLabel = new JLabel("0 orders | 0 alerts");
    private final DefaultTableModel branchSalesModel = new DefaultTableModel(new Object[]{"Branch", "Sales (KES)"}, 0);
    private final DefaultTableModel ordersModel = new DefaultTableModel(
            new Object[]{"Order ID", "Customer", "Branch", "Items", "Total", "Time"}, 0
    );
    private final DefaultTableModel stockModel = new DefaultTableModel(new Object[]{"Branch", "Drink", "Available"}, 0);
    private final JTextArea alertsArea = new JTextArea();

    public AdminDashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(UiTheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildAlertsArea(), BorderLayout.SOUTH);

        alertsArea.setEditable(false);
        UiTheme.styleArea(alertsArea);
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);

        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        UiTheme.styleCard(hero);

        JLabel title = new JLabel("Admin Control Dashboard");
        title.setFont(UiTheme.titleFont());
        JLabel subtitle = new JLabel("Monitor branch activity, stock positions, and reports from the live database.");
        subtitle.setFont(UiTheme.bodyFont());
        subtitle.setForeground(UiTheme.MUTED);

        hero.add(title);
        hero.add(Box.createVerticalStrut(6));
        hero.add(subtitle);

        JPanel actions = new JPanel(new GridLayout(2, 1, 10, 10));
        actions.setOpaque(false);
        actions.add(buildConnectionCard());
        actions.add(buildSummaryCard());

        panel.add(hero, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildConnectionCard() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        UiTheme.styleCard(panel);

        UiTheme.styleField(hostField);
        UiTheme.styleField(portField);

        JButton pingButton = new JButton("Ping");
        JButton loadButton = new JButton("Load Report");
        UiTheme.styleSecondaryButton(pingButton);
        UiTheme.stylePrimaryButton(loadButton);

        pingButton.addActionListener(event -> pingServer());
        loadButton.addActionListener(event -> loadReport());

        panel.add(new JLabel("Server Host"));
        panel.add(hostField);
        panel.add(new JLabel("Port"));
        panel.add(portField);
        panel.add(pingButton);
        panel.add(loadButton);
        return panel;
    }

    private JPanel buildSummaryCard() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        UiTheme.styleCard(panel);
        panel.add(createMetricCard("Status", statusLabel));
        panel.add(createMetricCard("Total Sales", totalSalesLabel));
        panel.add(createMetricCard("Orders / Alerts", countsLabel));
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(UiTheme.PANEL);
        panel.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(UiTheme.MUTED);
        titleLabel.setFont(UiTheme.bodyFont());
        valueLabel.setFont(UiTheme.sectionFont());
        valueLabel.setForeground(UiTheme.PRIMARY_DARK);
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);
        return panel;
    }

    private JSplitPane buildCenter() {
        JTable branchSalesTable = new JTable(branchSalesModel);
        JTable ordersTable = new JTable(ordersModel);
        JTable stockTable = new JTable(stockModel);

        branchSalesTable.setRowHeight(26);
        ordersTable.setRowHeight(26);
        stockTable.setRowHeight(26);

        JPanel left = new JPanel(new BorderLayout(12, 12));
        UiTheme.styleCard(left);
        left.add(new JLabel("Sales By Branch"), BorderLayout.NORTH);
        left.add(new JScrollPane(branchSalesTable), BorderLayout.CENTER);

        JPanel rightTop = new JPanel(new BorderLayout(12, 12));
        UiTheme.styleCard(rightTop);
        rightTop.add(new JLabel("Orders"), BorderLayout.NORTH);
        rightTop.add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        JPanel rightBottom = new JPanel(new BorderLayout(12, 12));
        UiTheme.styleCard(rightBottom);
        rightBottom.add(new JLabel("Current Stock"), BorderLayout.NORTH);
        rightBottom.add(new JScrollPane(stockTable), BorderLayout.CENTER);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightTop, rightBottom);
        rightSplit.setResizeWeight(0.55);
        rightSplit.setBorder(null);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, rightSplit);
        mainSplit.setResizeWeight(0.28);
        mainSplit.setBorder(null);
        return mainSplit;
    }

    private JScrollPane buildAlertsArea() {
        JScrollPane scrollPane = new JScrollPane(alertsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Low Stock Alerts"));
        scrollPane.setPreferredSize(new Dimension(0, 150));
        return scrollPane;
    }

    private void pingServer() {
        runRequest(() -> {
            try (ServerConnection connection = createConnection()) {
                return connection.send(new Request(RequestType.PING, null));
            }
        }, response -> statusLabel.setText(response.getMessage()));
    }

    private void loadReport() {
        runRequest(() -> {
            try (ServerConnection connection = createConnection()) {
                return connection.send(new Request(RequestType.GET_REPORT, null));
            }
        }, response -> populateReport((AdminReport) response.getPayload()));
    }

    private void runRequest(RequestTask task, ResponseHandler successHandler) {
        new SwingWorker<Response, Void>() {
            @Override
            protected Response doInBackground() throws Exception {
                return task.run();
            }

            @Override
            protected void done() {
                try {
                    Response response = get();
                    if (!response.isSuccess()) {
                        JOptionPane.showMessageDialog(AdminDashboardPanel.this, response.getMessage(),
                                "Request Failed", JOptionPane.ERROR_MESSAGE);
                        statusLabel.setText("Error");
                        return;
                    }
                    successHandler.handle(response);
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(AdminDashboardPanel.this, exception.getMessage(),
                            "Connection Error", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Disconnected");
                }
            }
        }.execute();
    }

    private void populateReport(AdminReport report) {
        statusLabel.setText("Connected");
        totalSalesLabel.setText("KES " + String.format("%.2f", report.getTotalBusinessSales()));
        countsLabel.setText(report.getOrders().size() + " orders | " + report.getAlerts().size() + " alerts");

        branchSalesModel.setRowCount(0);
        for (BranchSales branchSales : report.getBranchSales()) {
            branchSalesModel.addRow(new Object[]{
                    branchSales.getBranch().getDisplayName(),
                    String.format("%.2f", branchSales.getSalesAmount())
            });
        }

        ordersModel.setRowCount(0);
        for (OrderRecord order : report.getOrders()) {
            ordersModel.addRow(new Object[]{
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getBranch().getDisplayName(),
                    formatItems(order),
                    String.format("%.2f", order.getTotalAmount()),
                    order.getOrderTime().format(FORMATTER)
            });
        }

        stockModel.setRowCount(0);
        for (Map.Entry<Branch, Map<DrinkBrand, Integer>> branchEntry : report.getStockSnapshot().entrySet()) {
            for (Map.Entry<DrinkBrand, Integer> stockEntry : branchEntry.getValue().entrySet()) {
                stockModel.addRow(new Object[]{
                        branchEntry.getKey().getDisplayName(),
                        stockEntry.getKey().getDisplayName(),
                        stockEntry.getValue()
                });
            }
        }

        alertsArea.setText("");
        if (report.getAlerts().isEmpty()) {
            alertsArea.setText("No low-stock alerts at the moment.");
        } else {
            for (String alert : report.getAlerts()) {
                alertsArea.append(alert + System.lineSeparator());
            }
        }
    }

    private String formatItems(OrderRecord order) {
        StringBuilder builder = new StringBuilder();
        for (OrderItem item : order.getItems()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(item.getDrinkBrand().name()).append(" x").append(item.getQuantity());
        }
        return builder.toString();
    }

    private ServerConnection createConnection() throws Exception {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        return new ServerConnection(host, port);
    }

    @FunctionalInterface
    private interface RequestTask {
        Response run() throws Exception;
    }

    @FunctionalInterface
    private interface ResponseHandler {
        void handle(Response response);
    }
}

package com.dds.client;

import com.dds.shared.Branch;
import com.dds.shared.CustomerOrder;
import com.dds.shared.DrinkBrand;
import com.dds.shared.OrderItem;
import com.dds.shared.Request;
import com.dds.shared.RequestType;
import com.dds.shared.Response;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BranchClientPanel extends JPanel {
    private final JTextField hostField = new JTextField("127.0.0.1");
    private final JTextField portField = new JTextField("5050");
    private final JLabel connectionStatus = new JLabel("Disconnected");
    private final JTextField customerNameField = new JTextField();
    private final JComboBox<Branch> branchCombo = new JComboBox<>(Branch.values());
    private final JTextArea resultArea = new JTextArea(6, 24);
    private final Map<DrinkBrand, JLabel> priceLabels = new EnumMap<>(DrinkBrand.class);
    private final Map<DrinkBrand, JSpinner> quantitySpinners = new EnumMap<>(DrinkBrand.class);
    private Map<DrinkBrand, Double> menu = new EnumMap<>(DrinkBrand.class);

    public BranchClientPanel() {
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setBackground(UiTheme.BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        resultArea.setEditable(false);
        UiTheme.styleArea(resultArea);
        loadFallbackMenu();
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.setOpaque(false);

        JPanel hero = new JPanel();
        hero.setLayout(new BoxLayout(hero, BoxLayout.Y_AXIS));
        UiTheme.styleCard(hero);

        JLabel title = new JLabel("Branch Ordering Desk");
        title.setFont(UiTheme.titleFont());
        JLabel subtitle = new JLabel("Place live branch orders through the HQ socket server and database.");
        subtitle.setFont(UiTheme.bodyFont());
        subtitle.setForeground(UiTheme.MUTED);

        hero.add(title);
        hero.add(Box.createVerticalStrut(6));
        hero.add(subtitle);

        wrapper.add(hero, BorderLayout.CENTER);
        wrapper.add(buildConnectionCard(), BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildConnectionCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiTheme.styleCard(panel);
        panel.setPreferredSize(new Dimension(340, 150));

        UiTheme.styleField(hostField);
        UiTheme.styleField(portField);

        JButton pingButton = new JButton("Check Server");
        JButton menuButton = new JButton("Refresh Menu");
        UiTheme.styleSecondaryButton(pingButton);
        UiTheme.stylePrimaryButton(menuButton);

        pingButton.addActionListener(event -> pingServer());
        menuButton.addActionListener(event -> loadMenuFromServer());

        GridBagConstraints gbc = createGbc();
        panel.add(new JLabel("Server Host"), gbc);
        gbc.gridx = 1;
        panel.add(hostField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Port"), gbc);
        gbc.gridx = 1;
        panel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Status"), gbc);
        gbc.gridx = 1;
        connectionStatus.setForeground(UiTheme.PRIMARY_DARK);
        panel.add(connectionStatus, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(pingButton, gbc);
        gbc.gridx = 1;
        panel.add(menuButton, gbc);
        return panel;
    }

    private JPanel buildCenter() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setOpaque(false);
        panel.add(buildOrderCard(), BorderLayout.CENTER);
        panel.add(buildMenuCard(), BorderLayout.EAST);
        return panel;
    }

    private JPanel buildOrderCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiTheme.styleCard(panel);

        GridBagConstraints gbc = createGbc();
        gbc.gridwidth = 2;
        JLabel sectionTitle = new JLabel("Order Details");
        sectionTitle.setFont(UiTheme.sectionFont());
        panel.add(sectionTitle, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Customer Name"), gbc);
        gbc.gridx = 1;
        UiTheme.styleField(customerNameField);
        customerNameField.setPreferredSize(new Dimension(260, 34));
        panel.add(customerNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Branch"), gbc);
        gbc.gridx = 1;
        branchCombo.setFont(UiTheme.bodyFont());
        branchCombo.setPreferredSize(new Dimension(260, 34));
        panel.add(branchCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JLabel drinksTitle = new JLabel("Drink Quantities");
        drinksTitle.setFont(UiTheme.sectionFont());
        panel.add(drinksTitle, gbc);

        int row = gbc.gridy + 1;
        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            panel.add(new JLabel(drinkBrand.getDisplayName()), gbc);

            gbc.gridx = 1;
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, 1000, 1));
            spinner.setFont(UiTheme.bodyFont());
            spinner.setPreferredSize(new Dimension(90, 34));
            quantitySpinners.put(drinkBrand, spinner);
            panel.add(spinner, gbc);
            row++;
        }

        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton placeOrderButton = new JButton("Submit Order");
        UiTheme.stylePrimaryButton(placeOrderButton);
        placeOrderButton.addActionListener(event -> submitOrder());
        panel.add(placeOrderButton, gbc);
        return panel;
    }

    private JPanel buildMenuCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        UiTheme.styleCard(panel);
        panel.setPreferredSize(new Dimension(320, 0));

        GridBagConstraints gbc = createGbc();
        gbc.gridwidth = 2;
        JLabel title = new JLabel("Live Price Board");
        title.setFont(UiTheme.sectionFont());
        panel.add(title, gbc);

        int row = 1;
        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            gbc.gridy = row;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            panel.add(new JLabel(drinkBrand.getDisplayName()), gbc);

            gbc.gridx = 1;
            JLabel priceLabel = new JLabel("KES 0.00");
            priceLabel.setForeground(UiTheme.ACCENT);
            priceLabel.setFont(UiTheme.sectionFont());
            priceLabels.put(drinkBrand, priceLabel);
            panel.add(priceLabel, gbc);
            row++;
        }
        return panel;
    }

    private JScrollPane buildFooter() {
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Order Activity"));
        scrollPane.setPreferredSize(new Dimension(0, 160));
        return scrollPane;
    }

    private void pingServer() {
        runRequest("Checking server...", () -> {
            try (ServerConnection connection = createConnection()) {
                return connection.send(new Request(RequestType.PING, null));
            }
        }, response -> connectionStatus.setText(response.getMessage()));
    }

    private void loadMenuFromServer() {
        runRequest("Loading menu from server...", () -> {
            try (ServerConnection connection = createConnection()) {
                return connection.send(new Request(RequestType.GET_MENU, null));
            }
        }, response -> {
            @SuppressWarnings("unchecked")
            Map<DrinkBrand, Double> liveMenu = (Map<DrinkBrand, Double>) response.getPayload();
            menu = new EnumMap<>(liveMenu);
            updatePriceBoard();
            connectionStatus.setText("Connected");
        });
    }

    private void submitOrder() {
        String customerName = customerNameField.getText().trim();
        Branch branch = (Branch) branchCombo.getSelectedItem();
        List<OrderItem> items = new ArrayList<>();

        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            int quantity = (Integer) quantitySpinners.get(drinkBrand).getValue();
            if (quantity > 0) {
                items.add(new OrderItem(drinkBrand, quantity));
            }
        }

        if (customerName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the customer name first.", "Missing Name",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one drink quantity.", "No Items",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CustomerOrder order = new CustomerOrder(customerName, branch, items);
        runRequest("Submitting order to HQ server...", () -> {
            try (ServerConnection connection = createConnection()) {
                return connection.send(new Request(RequestType.PLACE_ORDER, order));
            }
        }, response -> {
            appendActivity(response.getMessage());
            for (JSpinner spinner : quantitySpinners.values()) {
                spinner.setValue(0);
            }
            customerNameField.setText("");
            connectionStatus.setText("Order synced");
        });
    }

    private void runRequest(String startMessage, RequestTask task, ResponseHandler successHandler) {
        appendActivity(startMessage);
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
                        appendActivity("Failed: " + response.getMessage());
                        connectionStatus.setText("Error");
                        return;
                    }
                    successHandler.handle(response);
                    appendActivity(response.getMessage());
                } catch (Exception exception) {
                    appendActivity("Connection error: " + exception.getMessage());
                    connectionStatus.setText("Disconnected");
                }
            }
        }.execute();
    }

    private ServerConnection createConnection() throws Exception {
        String host = hostField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        return new ServerConnection(host, port);
    }

    private void loadFallbackMenu() {
        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            menu.put(drinkBrand, drinkBrand.getUnitPrice());
        }
        updatePriceBoard();
    }

    private void updatePriceBoard() {
        for (DrinkBrand drinkBrand : DrinkBrand.values()) {
            Double price = menu.getOrDefault(drinkBrand, drinkBrand.getUnitPrice());
            priceLabels.get(drinkBrand).setText("KES " + String.format("%.2f", price));
        }
    }

    private void appendActivity(String message) {
        resultArea.append(message + System.lineSeparator());
        resultArea.setCaretPosition(resultArea.getDocument().getLength());
    }

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        return gbc;
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

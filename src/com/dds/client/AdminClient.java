package com.dds.client;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class AdminClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();

            JFrame frame = new JFrame("DDS Admin Dashboard");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new AdminDashboardPanel(), BorderLayout.CENTER);
            frame.setSize(1200, 780);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

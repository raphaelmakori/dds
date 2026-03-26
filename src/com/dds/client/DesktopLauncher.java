package com.dds.client;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class DesktopLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();

            JFrame frame = new JFrame("Distributed Drinks Sales System");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JTabbedPane tabs = new JTabbedPane();
            tabs.setFont(UiTheme.bodyFont());
            tabs.addTab("Branch Desk", new BranchClientPanel());
            tabs.addTab("Admin Dashboard", new AdminDashboardPanel());

            frame.add(tabs, BorderLayout.CENTER);
            frame.setSize(1280, 820);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

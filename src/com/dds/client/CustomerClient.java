package com.dds.client;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;

public class CustomerClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UiTheme.install();

            JFrame frame = new JFrame("DDS Branch Ordering Desk");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(new BranchClientPanel(), BorderLayout.CENTER);
            frame.setSize(1080, 760);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}

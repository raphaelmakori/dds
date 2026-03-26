package com.dds.client;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class UiTheme {
    public static final Color BACKGROUND = new Color(244, 247, 242);
    public static final Color PANEL = new Color(255, 255, 255);
    public static final Color PRIMARY = new Color(26, 99, 79);
    public static final Color PRIMARY_DARK = new Color(18, 72, 58);
    public static final Color ACCENT = new Color(227, 145, 59);
    public static final Color BORDER = new Color(210, 220, 214);
    public static final Color TEXT = new Color(31, 42, 36);
    public static final Color MUTED = new Color(101, 117, 108);

    private static final Font TITLE_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    private static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);

    private UiTheme() {
    }

    public static void install() {
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("Label.foreground", TEXT);
        UIManager.put("TabbedPane.selected", PANEL);
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        );
    }

    public static void styleCard(JComponent component) {
        component.setBackground(PANEL);
        component.setBorder(cardBorder());
    }

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        button.setFont(BODY_FONT);
    }

    public static void styleSecondaryButton(JButton button) {
        button.setBackground(new Color(233, 239, 234));
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
        button.setFont(BODY_FONT);
    }

    public static void styleField(JTextField field) {
        field.setFont(BODY_FONT);
        field.setPreferredSize(new Dimension(160, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    public static void styleArea(JTextArea area) {
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setForeground(TEXT);
        area.setBackground(PANEL);
        area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
    }

    public static Font titleFont() {
        return TITLE_FONT;
    }

    public static Font sectionFont() {
        return SECTION_FONT;
    }

    public static Font bodyFont() {
        return BODY_FONT;
    }
}

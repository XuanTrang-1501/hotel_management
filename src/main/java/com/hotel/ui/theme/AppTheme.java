package com.hotel.ui.theme;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

public final class AppTheme {
    public static final Color SIDEBAR = new Color(16, 33, 51);
    public static final Color SIDEBAR_DEEP = new Color(9, 20, 33);
    public static final Color SIDEBAR_HOVER = new Color(27, 52, 76);
    public static final Color SIDEBAR_ACTIVE = new Color(35, 68, 99);
    public static final Color GOLD = new Color(203, 165, 47);
    public static final Color GOLD_DARK = new Color(166, 128, 24);
    public static final Color CREAM = new Color(244, 246, 250);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(23, 30, 43);
    public static final Color MUTED = new Color(112, 120, 134);
    public static final Color BORDER = new Color(228, 233, 240);
    public static final Color SHADOW = new Color(15, 23, 42, 26);
    public static final Color STRIPE = new Color(249, 250, 252);
    public static final Color SUCCESS = new Color(15, 118, 110);
    public static final Color SUCCESS_BG = new Color(204, 251, 241);
    public static final Color WARNING = new Color(180, 83, 9);
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color DANGER = new Color(185, 28, 28);
    public static final Color DANGER_BG = new Color(254, 226, 226);
    public static final Color INFO = new Color(29, 78, 216);
    public static final Color INFO_BG = new Color(219, 234, 254);

    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font SUBHEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BUTTON = new Font("Segoe UI", Font.BOLD, 14);

    private AppTheme() {}

    public static void install() {
        FlatLightLaf.setup();
        FlatLaf.setUseNativeWindowDecorations(true);
        UIManager.put("defaultFont", new FontUIResource(BODY));
        UIManager.put("Component.arc", 12);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 1);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("ProgressBar.arc", 10);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.showButtons", false);
        UIManager.put("ScrollBar.width", 12);
        UIManager.put("TabbedPane.selectedBackground", CARD);
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.alternateRowColor", STRIPE);
        UIManager.put("Table.selectionBackground", new Color(232, 244, 248));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("Table.rowHeight", 38);
        UIManager.put("TableHeader.height", 40);
        UIManager.put("TableHeader.separatorColor", BORDER);
        UIManager.put("TableHeader.bottomSeparatorColor", BORDER);
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BUTTON);
        UIManager.put("ToolTip.background", SIDEBAR);
        UIManager.put("ToolTip.foreground", Color.WHITE);
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BUTTON);
        b.setForeground(Color.WHITE);
        b.setBackground(SIDEBAR);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        return b;
    }

    public static JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BUTTON);
        b.setForeground(SIDEBAR);
        b.setBackground(GOLD);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        return b;
    }

    public static JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BUTTON);
        b.setForeground(SIDEBAR);
        b.setBackground(new Color(241, 245, 249));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        return b;
    }

    public static JButton dangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(BUTTON);
        b.setForeground(Color.WHITE);
        b.setBackground(DANGER);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        return b;
    }

    public static JTextField field(int columns) {
        JTextField f = new JTextField(columns);
        styleField(f);
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        styleField(f);
        return f;
    }

    public static void styleField(JComponent f) {
        f.setFont(BODY);
        f.setBackground(new Color(248, 250, 252));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
    }

    public static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(TEXT);
        return l;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(40);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(232, 244, 248));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(SUBHEADING);
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 252));
        header.setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, center);
    }

    public static JScrollPane wrapTable(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER, 1, true));
        sp.getViewport().setBackground(CARD);
        return sp;
    }

    public static Color statusForeground(String status) {
        if (status == null) return MUTED;
        return switch (status.toUpperCase()) {
            case "AVAILABLE", "CONFIRMED", "CHECKED_IN", "PAID", "ACTIVE" -> SUCCESS;
            case "OCCUPIED", "PENDING", "MAINTENANCE", "PARTIAL", "UNPAID" -> WARNING;
            case "CANCELLED", "CHECKED_OUT", "INACTIVE" -> DANGER;
            default -> INFO;
        };
    }

    public static Color statusBackground(String status) {
        if (status == null) return new Color(243, 244, 246);
        return switch (status.toUpperCase()) {
            case "AVAILABLE", "CONFIRMED", "CHECKED_IN", "PAID", "ACTIVE" -> SUCCESS_BG;
            case "OCCUPIED", "PENDING", "MAINTENANCE", "PARTIAL", "UNPAID" -> WARNING_BG;
            case "CANCELLED", "CHECKED_OUT", "INACTIVE" -> DANGER_BG;
            default -> INFO_BG;
        };
    }

    public static String statusLabel(String status) {
        if (status == null) return "";
        return switch (status.toUpperCase()) {
            case "AVAILABLE" -> "Trống";
            case "OCCUPIED" -> "Đang dùng";
            case "MAINTENANCE" -> "Bảo trì";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PENDING" -> "Chờ xử lý";
            case "CANCELLED" -> "Đã hủy";
            case "CHECKED_IN" -> "Đã nhận phòng";
            case "CHECKED_OUT" -> "Đã trả phòng";
            case "PAID" -> "Đã thanh toán";
            case "UNPAID" -> "Chưa thanh toán";
            case "PARTIAL" -> "Thanh toán một phần";
            case "INACTIVE" -> "Ngưng dùng";
            default -> status;
        };
    }

    public static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fill;
        private Color borderColor;
        private boolean shadow;

        public RoundedPanel(int radius, Color fill) {
            this(radius, fill, false);
        }

        public RoundedPanel(int radius, Color fill, boolean shadow) {
            this.radius = radius;
            this.fill = fill;
            this.shadow = shadow;
            setOpaque(false);
        }

        public void setFill(Color fill) {
            this.fill = fill;
            repaint();
        }

        public void setBorderColor(Color borderColor) {
            this.borderColor = borderColor;
            repaint();
        }

        public void setShadow(boolean shadow) {
            this.shadow = shadow;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            int m = shadow ? 9 : 0;
            if (shadow) {
                for (int i = m + 3; i >= 1; i--) {
                    g2.setColor(SHADOW);
                    g2.fill(new RoundRectangle2D.Float(i, i + 3f, w - 2 * i, h - 2 * i, radius + 4, radius + 4));
                }
            }
            g2.setColor(fill);
            g2.fill(new RoundRectangle2D.Float(m, m, w - 2 * m, h - 2 * m, radius, radius));
            if (borderColor != null) {
                g2.setColor(borderColor);
                g2.setStroke(new java.awt.BasicStroke(1f));
                g2.draw(new RoundRectangle2D.Float(m + 0.5f, m + 0.5f, w - 2 * m - 1, h - 2 * m - 1, radius, radius));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

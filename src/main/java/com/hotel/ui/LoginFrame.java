package com.hotel.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.hotel.dao.UserDAO;
import com.hotel.service.AuthService;
import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.AppTheme.RoundedPanel;

public class LoginFrame extends JFrame {
    private final JTextField username = AppTheme.field(18);
    private final JPasswordField password = AppTheme.passwordField(18);

    public LoginFrame() {
        setTitle("Lumina Hotel — Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(920, 560);
        setMinimumSize(new Dimension(820, 520));
        setExtendedState(MAXIMIZED_BOTH);
        setResizable(true);

        username.setText("");
        password.setEchoChar('•');

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(brandPanel());
        root.add(formPanel());
        setContentPane(root);
    }

    private JPanel brandPanel() {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, AppTheme.SIDEBAR, getWidth(), getHeight(), new Color(10, 24, 40));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(201, 162, 39, 40));
                g2.fillOval(-80, getHeight() - 220, 280, 280);
                g2.setColor(new Color(201, 162, 39, 22));
                g2.fillOval(getWidth() - 160, -60, 240, 240);
                g2.dispose();
            }
        };
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(48, 48, 48, 48));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JLabel mark = new JLabel("◆  LUMINA");
        mark.setFont(new Font("Segoe UI", Font.BOLD, 14));
        mark.setForeground(AppTheme.GOLD);
        mark.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel title = new JLabel("<html>Hệ thống<br>quản lý khách sạn</html>");
        title.setFont(new Font("Segoe UI", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Điều hành phòng, khách hàng và đặt phòng<br>trên một không gian làm việc hiện đại.</html>");
        tagline.setFont(AppTheme.BODY);
        tagline.setForeground(new Color(203, 213, 225));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(mark);
        inner.add(Box.createVerticalStrut(18));
        inner.add(title);
        inner.add(Box.createVerticalStrut(16));
        inner.add(tagline);
        inner.add(Box.createVerticalStrut(36));

        JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        chips.add(chip("Phòng"));
        chips.add(chip("Khách hàng"));
        chips.add(chip("Đặt phòng"));
        inner.add(chips);

        GridBagConstraints g = new GridBagConstraints();
        g.anchor = GridBagConstraints.WEST;
        p.add(inner, g);
        return p;
    }

    private JComponent chip(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.SMALL);
        l.setForeground(Color.WHITE);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(201, 162, 39, 90), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        return l;
    }

    private JPanel formPanel() {
        JPanel wrap = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, AppTheme.CREAM, 0, getHeight(), Color.WHITE);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        RoundedPanel card = new RoundedPanel(22, AppTheme.CARD, true);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 36, 40));
        card.setPreferredSize(new Dimension(390, 462));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 8, 0);

        JLabel hello = new JLabel("Chào mừng trở lại");
        hello.setFont(new Font("Segoe UI", Font.BOLD, 24));
        hello.setForeground(AppTheme.TEXT);
        g.gridy = 0;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(hello, g);

        JLabel hint = AppTheme.muted("Đăng nhập để tiếp tục quản lý khách sạn.");
        g.gridy = 1;
        g.insets = new Insets(0, 0, 26, 0);
        card.add(hint, g);

        g.gridy = 2;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(label("Tên đăng nhập"), g);
        g.gridy = 3;
        g.insets = new Insets(0, 0, 18, 0);
        card.add(username, g);

        g.gridy = 4;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(label("Mật khẩu"), g);
        g.gridy = 5;
        g.insets = new Insets(0, 0, 6, 0);
        card.add(password, g);

        JCheckBox showPassword = new JCheckBox("Hiển thị mật khẩu");
        showPassword.setFont(AppTheme.SMALL);
        showPassword.setForeground(AppTheme.MUTED);
        showPassword.setOpaque(false);
        showPassword.setFocusPainted(false);
        showPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPassword.addActionListener(e ->
                password.setEchoChar(showPassword.isSelected() ? (char) 0 : '•'));
        g.gridy = 6;
        g.insets = new Insets(0, 0, 20, 0);
        card.add(showPassword, g);

        JButton login = AppTheme.accentButton("Đăng nhập");
        login.setPreferredSize(new Dimension(300, 46));
        login.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        g.gridy = 7;
        g.insets = new Insets(0, 0, 18, 0);
        card.add(login, g);

        JLabel footer = new JLabel("© 2026 Lumina Hotel · Hệ thống quản lý khách sạn");
        footer.setFont(AppTheme.SMALL);
        footer.setForeground(AppTheme.MUTED);
        footer.setHorizontalAlignment(JLabel.CENTER);
        g.gridy = 8;
        g.insets = new Insets(0, 0, 0, 0);
        card.add(footer, g);

        wrap.add(card);
        return wrap;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(AppTheme.SMALL);
        l.setForeground(AppTheme.MUTED);
        return l;
    }

    private void doLogin() {
        try {
            AuthService.Session session = new AuthService(new UserDAO()).login(username.getText().trim(), new String(password.getPassword()));
            if (session == null) {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu.", "Không thể đăng nhập", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dispose();
            new DashboardFrame(session).setVisible(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

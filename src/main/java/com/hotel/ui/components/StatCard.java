package com.hotel.ui.components;

import com.hotel.ui.theme.AppTheme;
import com.hotel.ui.theme.AppTheme.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class StatCard extends RoundedPanel {
    private final JLabel valueLabel;
    private final JLabel hintLabel;

    public StatCard(String title, String value, String hint, Color accent) {
        super(18, AppTheme.CARD, true);
        setLayout(new BorderLayout(14, 0));
        setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JPanel icon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 34));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.setColor(accent);
                int d = 10;
                g2.fillOval((getWidth() - d) / 2, (getHeight() - d) / 2, d, d);
                g2.dispose();
            }
        };
        icon.setOpaque(false);
        icon.setPreferredSize(new Dimension(42, 42));

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(AppTheme.SMALL);
        titleLabel.setForeground(AppTheme.MUTED);

        valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(AppTheme.TEXT);

        hintLabel = new JLabel(hint);
        hintLabel.setFont(AppTheme.SMALL);
        hintLabel.setForeground(AppTheme.MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        hintLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(titleLabel);
        text.add(Box.createVerticalStrut(8));
        text.add(valueLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(hintLabel);

        add(icon, BorderLayout.WEST);
        add(text, BorderLayout.CENTER);
    }

    public void setValue(String value) {
        valueLabel.setText(value);
    }

    public void setHint(String hint) {
        hintLabel.setText(hint);
    }
}

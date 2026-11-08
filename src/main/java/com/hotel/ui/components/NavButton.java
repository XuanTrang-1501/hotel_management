package com.hotel.ui.components;

import com.hotel.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class NavButton extends JButton {
    private boolean active;
    private boolean hover;

    public NavButton(String text) {
        super(text);
        setHorizontalAlignment(LEFT);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(AppTheme.SUBHEADING);
        setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 18));
        setForeground(new Color(206, 216, 230));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hover = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                repaint();
            }
        });
    }

    public void setActive(boolean active) {
        this.active = active;
        setForeground(active ? AppTheme.GOLD : new Color(206, 216, 230));
        repaint();
    }

    public boolean isActive() {
        return active;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        if (active) {
            g2.setColor(AppTheme.SIDEBAR_ACTIVE);
            g2.fillRoundRect(0, 0, w, h, 12, 12);
            g2.setColor(AppTheme.GOLD);
            g2.fillRoundRect(0, h / 2 - 11, 3, 22, 3, 3);
        } else if (hover) {
            g2.setColor(AppTheme.SIDEBAR_HOVER);
            g2.fillRoundRect(0, 0, w, h, 12, 12);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}

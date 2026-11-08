package com.hotel.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import com.hotel.model.ChartPoint;
import com.hotel.ui.theme.AppTheme;

public class BarChartPanel extends JPanel {
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0");
    private static final int PADDING_LEFT = 64;
    private static final int PADDING_RIGHT = 20;
    private static final int PADDING_TOP = 44;
    private static final int PADDING_BOTTOM = 48;

    private String title = "";
    private String unit = "";
    private List<ChartPoint> data = new ArrayList<>();
    private Color barColor = AppTheme.GOLD;

    public BarChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(760, 360));
    }

    public void setData(String title, String unit, List<ChartPoint> points, Color color) {
        this.title = title == null ? "" : title;
        this.unit = unit == null ? "" : unit;
        this.data = points == null ? new ArrayList<>() : new ArrayList<>(points);
        this.barColor = color == null ? AppTheme.GOLD : color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        g2.setColor(AppTheme.TEXT);
        g2.setFont(AppTheme.SUBHEADING);
        g2.drawString(title, PADDING_LEFT - 44, 24);
        if (!unit.isEmpty()) {
            g2.setFont(AppTheme.SMALL);
            g2.setColor(AppTheme.MUTED);
            g2.drawString(unit, PADDING_LEFT - 44 + g2.getFontMetrics(AppTheme.SUBHEADING).stringWidth(title) + 10, 24);
        }

        int plotX = PADDING_LEFT;
        int plotY = PADDING_TOP;
        int plotW = Math.max(w - PADDING_LEFT - PADDING_RIGHT, 10);
        int plotH = Math.max(h - PADDING_TOP - PADDING_BOTTOM, 10);

        g2.setColor(AppTheme.MUTED);
        g2.setFont(AppTheme.SMALL);
        if (data.isEmpty()) {
            g2.drawString("Chưa có dữ liệu trong khoảng đã chọn.", plotX, plotY + plotH / 2);
            g2.dispose();
            return;
        }

        double max = 0;
        for (ChartPoint p : data) {
            if (p.value() != null) max = Math.max(max, p.value().doubleValue());
        }
        if (max <= 0) max = 1;

        int ticks = 4;
        g2.setStroke(new BasicStroke(1f));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 0; i <= ticks; i++) {
            int y = plotY + plotH - (int) Math.round(plotH * i / (double) ticks);
            g2.setColor(i == 0 ? AppTheme.BORDER : new Color(241, 245, 249));
            g2.drawLine(plotX, y, plotX + plotW, y);
            g2.setColor(AppTheme.MUTED);
            String label = MONEY.format(max * i / ticks);
            g2.drawString(label, plotX - 8 - fm.stringWidth(label), y + 4);
        }

        int n = data.size();
        double slot = plotW / (double) n;
        int barWidth = (int) Math.max(2, Math.min(slot * 0.62, 46));
        int labelEvery = Math.max(1, (int) Math.ceil(n / 12.0));

        for (int i = 0; i < n; i++) {
            ChartPoint p = data.get(i);
            double value = p.value() == null ? 0 : p.value().doubleValue();
            int barH = (int) Math.round(plotH * (value / max));
            int x = (int) (plotX + slot * i + (slot - barWidth) / 2.0);
            int y = plotY + plotH - barH;

            g2.setColor(barColor);
            if (barH > 0) {
                g2.fill(new RoundRectangle2D.Float(x, y, barWidth, barH, 6, 6));
            }

            if (i % labelEvery == 0 || n <= 12) {
                g2.setColor(AppTheme.MUTED);
                String lbl = p.label() == null ? "" : p.label();
                int lw = fm.stringWidth(lbl);
                g2.drawString(lbl, x + barWidth / 2 - lw / 2, plotY + plotH + 18);
            }
        }

        g2.setColor(AppTheme.BORDER);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(plotX, plotY + plotH, plotX + plotW, plotY + plotH);
        g2.dispose();
    }
}

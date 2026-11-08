package com.hotel.ui.components;

import com.hotel.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class StatusCellRenderer extends DefaultTableCellRenderer {
    public StatusCellRenderer() {
        setHorizontalAlignment(CENTER);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                   boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String raw = value == null ? "" : value.toString();
        setText(AppTheme.statusLabel(raw));
        setFont(AppTheme.SMALL);
        setOpaque(true);
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            setForeground(AppTheme.statusForeground(raw));
        } else {
            setBackground(AppTheme.statusBackground(raw));
            setForeground(AppTheme.statusForeground(raw));
        }
        setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return this;
    }
}

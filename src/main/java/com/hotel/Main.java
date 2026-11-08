package com.hotel;

import com.hotel.ui.LoginFrame;
import com.hotel.ui.theme.AppTheme;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppTheme.install();
            new LoginFrame().setVisible(true);
        });
    }
}

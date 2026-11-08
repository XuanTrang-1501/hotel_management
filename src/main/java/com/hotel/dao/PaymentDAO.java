package com.hotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hotel.config.DatabaseConnection;
import com.hotel.repository.PaymentRepository;

public class PaymentDAO implements PaymentRepository {
    public BigDecimal totalPaid(long bookingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM payments WHERE booking_id=? AND status='PAID'";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    public void insert(long bookingId, BigDecimal amount, String method) throws SQLException {
        String sql = "INSERT INTO payments(booking_id,amount,method,status) VALUES(?,?,?,'PAID')";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.executeUpdate();
        }
    }

    public BigDecimal totalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='PAID'";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}

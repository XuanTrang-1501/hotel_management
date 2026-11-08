package com.hotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotel.config.DatabaseConnection;
import com.hotel.model.Invoice;
import com.hotel.repository.InvoiceRepository;

public class InvoiceDAO implements InvoiceRepository {
    private static final String SELECT_SQL = """
            SELECT i.id,i.invoice_code,b.booking_code,c.full_name,i.total_amount,i.issued_at
            FROM invoices i
            JOIN bookings b ON b.id=i.booking_id
            JOIN customers c ON c.id=b.customer_id
            """;

    public Invoice findByBooking(long bookingId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL + " WHERE i.booking_id=?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Invoice> findAll() throws SQLException {
        List<Invoice> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_SQL + " ORDER BY i.id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Invoice createIfAbsent(long bookingId, BigDecimal total) throws SQLException {
        Invoice existing = findByBooking(bookingId);
        if (existing != null) return existing;
        String sql = "INSERT INTO invoices(invoice_code,booking_id,total_amount) VALUES(?,?,?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "INV" + System.currentTimeMillis());
            ps.setLong(2, bookingId);
            ps.setBigDecimal(3, total);
            ps.executeUpdate();
        }
        return findByBooking(bookingId);
    }

    private Invoice map(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getLong("id"),
                rs.getString("invoice_code"),
                rs.getString("booking_code"),
                rs.getString("full_name"),
                rs.getBigDecimal("total_amount"),
                rs.getTimestamp("issued_at").toLocalDateTime()
        );
    }
}

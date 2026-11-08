package com.hotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.hotel.config.DatabaseConnection;
import com.hotel.model.ChartPoint;
import com.hotel.model.ReportSummary;
import com.hotel.repository.ReportRepository;

public class ReportDAO implements ReportRepository {

    @Override
    public List<ChartPoint> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT DATE(paid_at) d, COALESCE(SUM(amount),0) amount
                FROM payments
                WHERE status='PAID' AND DATE(paid_at) BETWEEN ? AND ?
                GROUP BY DATE(paid_at)
                ORDER BY d
                """;
        List<ChartPoint> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChartPoint(rs.getDate("d").toLocalDate().toString(), rs.getBigDecimal("amount")));
                }
            }
        }
        return list;
    }

    @Override
    public List<ChartPoint> revenueByMonth(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT DATE_FORMAT(paid_at,'%Y-%m') m, COALESCE(SUM(amount),0) amount
                FROM payments
                WHERE status='PAID' AND DATE(paid_at) BETWEEN ? AND ?
                GROUP BY DATE_FORMAT(paid_at,'%Y-%m')
                ORDER BY m
                """;
        List<ChartPoint> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new ChartPoint(rs.getString("m"), rs.getBigDecimal("amount")));
            }
        }
        return list;
    }

    @Override
    public List<ChartPoint> revenueByRoomType() throws SQLException {
        String sql = """
                SELECT t.type_name label, COALESCE(SUM(d.line_total),0) amount
                FROM booking_details d
                JOIN rooms r ON r.id=d.room_id
                JOIN room_types t ON t.id=r.room_type_id
                JOIN bookings b ON b.id=d.booking_id
                WHERE b.status <> 'CANCELLED'
                GROUP BY t.type_name
                ORDER BY amount DESC
                """;
        List<ChartPoint> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new ChartPoint(rs.getString("label"), rs.getBigDecimal("amount")));
        }
        return list;
    }

    @Override
    public ReportSummary summary(LocalDate from, LocalDate to) throws SQLException {
        String revenueSql =
                "SELECT COALESCE(SUM(amount),0) FROM payments WHERE status='PAID' AND DATE(paid_at) BETWEEN ? AND ?";
        String bookingsSql =
                "SELECT COUNT(*) FROM bookings WHERE status <> 'CANCELLED' AND check_in BETWEEN ? AND ?";
        String bookedSql = """
                SELECT COALESCE(SUM(DATEDIFF(LEAST(b.check_out, ?), GREATEST(b.check_in, ?))),0)
                FROM booking_details d JOIN bookings b ON b.id=d.booking_id
                WHERE b.status <> 'CANCELLED' AND b.check_in < ? AND b.check_out > ?
                """;
        String roomsSql = "SELECT COUNT(*) FROM rooms";
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            BigDecimal revenue;
            try (PreparedStatement ps = c.prepareStatement(revenueSql)) {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
                revenue = readDecimal(ps);
            }
            int bookings;
            try (PreparedStatement ps = c.prepareStatement(bookingsSql)) {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
                bookings = readDecimal(ps).intValue();
            }
            long booked;
            try (PreparedStatement ps = c.prepareStatement(bookedSql)) {
                ps.setDate(1, java.sql.Date.valueOf(to.plusDays(1)));
                ps.setDate(2, java.sql.Date.valueOf(from));
                ps.setDate(3, java.sql.Date.valueOf(to.plusDays(1)));
                ps.setDate(4, java.sql.Date.valueOf(from));
                booked = readDecimal(ps).longValue();
            }
            long rooms;
            try (PreparedStatement ps = c.prepareStatement(roomsSql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                rooms = rs.getLong(1);
            }
            long days = Math.max(ChronoUnit.DAYS.between(from, to) + 1, 0);
            return new ReportSummary(revenue, bookings, booked, rooms * days);
        }
    }

    private BigDecimal readDecimal(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getBigDecimal(1);
        }
    }
}

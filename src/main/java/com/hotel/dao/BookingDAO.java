package com.hotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.hotel.config.DatabaseConnection;
import com.hotel.model.Booking;
import com.hotel.model.BookingDetail;
import com.hotel.patterns.builder.BookingRequest;
import com.hotel.patterns.strategy.PricingStrategy;
import com.hotel.repository.BookingRepository;

public class BookingDAO implements BookingRepository {
    private static final String LIST_SQL = """
            SELECT b.id,b.booking_code,c.full_name,c.phone,b.check_in,b.check_out,b.status,
                   COALESCE(SUM(d.line_total),0) total,
                   COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.booking_id=b.id AND p.status='PAID'),0) paid
            FROM bookings b
            JOIN customers c ON c.id=b.customer_id
            LEFT JOIN booking_details d ON d.booking_id=b.id
            %s
            GROUP BY b.id,b.booking_code,c.full_name,c.phone,b.check_in,b.check_out,b.status
            ORDER BY b.id DESC
            """;

    public List<Booking> findAll() throws SQLException {
        List<Booking> list = new ArrayList<>();
        String sql = LIST_SQL.formatted("");
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Booking findById(long id) throws SQLException {
        String sql = LIST_SQL.formatted("WHERE b.id=?");
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy đặt phòng #" + id);
                return map(rs);
            }
        }
    }

    @Override
    public BigDecimal basePriceOf(long roomId) throws SQLException {
        String sql = "SELECT t.price_per_night FROM rooms r JOIN room_types t ON t.id=r.room_type_id WHERE r.id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy phòng #" + roomId);
                return rs.getBigDecimal(1);
            }
        }
    }

    @Override
    public void create(BookingRequest request, PricingStrategy pricing) throws SQLException {
        LocalDate in = request.checkIn();
        LocalDate out = request.checkOut();
        List<Long> roomIds = request.roomIds();
        String overlap = "SELECT COUNT(*) FROM booking_details d JOIN bookings b ON b.id=d.booking_id " +
                "WHERE d.room_id=? AND b.status NOT IN ('CANCELLED','CHECKED_OUT') AND b.check_in < ? AND b.check_out > ? FOR UPDATE";
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                long bookingId;
                String code = "BK" + System.currentTimeMillis();
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO bookings(booking_code,customer_id,created_by,check_in,check_out,status,note) VALUES(?,?,?,?,?,'CONFIRMED',?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, code);
                    ps.setLong(2, request.customerId());
                    if (request.userId() > 0) ps.setLong(3, request.userId());
                    else ps.setNull(3, Types.BIGINT);
                    ps.setDate(4, java.sql.Date.valueOf(in));
                    ps.setDate(5, java.sql.Date.valueOf(out));
                    ps.setString(6, "Chính sách giá: " + pricing.name());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        bookingId = keys.getLong(1);
                    }
                }
                int nights = (int) ChronoUnit.DAYS.between(in, out);
                for (long roomId : roomIds) {
                    BigDecimal basePrice;
                    try (PreparedStatement lock = c.prepareStatement(
                            "SELECT t.price_per_night FROM rooms r JOIN room_types t ON t.id=r.room_type_id " +
                                    "WHERE r.id=? AND r.status IN ('AVAILABLE','OCCUPIED') FOR UPDATE")) {
                        lock.setLong(1, roomId);
                        try (ResultSet r = lock.executeQuery()) {
                            if (!r.next()) throw new SQLException("Phòng #" + roomId + " không tồn tại hoặc không hoạt động.");
                            basePrice = r.getBigDecimal(1);
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(overlap)) {
                        ps.setLong(1, roomId);
                        ps.setDate(2, java.sql.Date.valueOf(out));
                        ps.setDate(3, java.sql.Date.valueOf(in));
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            if (rs.getInt(1) > 0) throw new SQLException("Phòng #" + roomId + " đã được đặt trong khoảng thời gian này.");
                        }
                    }
                    BigDecimal lineTotal = pricing.calculate(basePrice, in, out);
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO booking_details(booking_id,room_id,price_per_night,nights,line_total) VALUES(?,?,?,?,?)")) {
                        ps.setLong(1, bookingId);
                        ps.setLong(2, roomId);
                        ps.setBigDecimal(3, basePrice);
                        ps.setInt(4, nights);
                        ps.setBigDecimal(5, lineTotal);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    @Override
    public List<BookingDetail> findDetails(long bookingId) throws SQLException {
        String sql = """
                SELECT d.id,d.booking_id,d.room_id,r.room_number,t.type_name,
                       d.price_per_night,d.nights,d.line_total
                FROM booking_details d
                JOIN rooms r ON r.id=d.room_id
                JOIN room_types t ON t.id=r.room_type_id
                WHERE d.booking_id=?
                ORDER BY d.id
                """;
        List<BookingDetail> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BookingDetail(rs.getLong("id"), rs.getLong("booking_id"), rs.getLong("room_id"),
                            rs.getString("room_number"), rs.getString("type_name"),
                            rs.getBigDecimal("price_per_night"), rs.getInt("nights"), rs.getBigDecimal("line_total")));
                }
            }
        }
        return list;
    }

    @Override
    public long splitRoom(long bookingId, long detailId) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                BookMeta meta = lockMeta(c, bookingId);
                requireSplittable(meta.status());
                if (hasPaid(c, bookingId)) {
                    throw new SQLException("Đơn đã phát sinh thanh toán, không thể tách.");
                }
                if (detailCount(c, bookingId) <= 1) {
                    throw new SQLException("Đơn chỉ có một phòng, không cần tách.");
                }
                long newId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO bookings(booking_code,customer_id,created_by,check_in,check_out,status,note) " +
                                "VALUES(?,?,NULL,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, "BK" + System.nanoTime());
                    ps.setLong(2, meta.customerId());
                    ps.setDate(3, java.sql.Date.valueOf(meta.checkIn()));
                    ps.setDate(4, java.sql.Date.valueOf(meta.checkOut()));
                    ps.setString(5, meta.status());
                    ps.setString(6, "Tách từ đơn #" + bookingId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        newId = keys.getLong(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE booking_details SET booking_id=? WHERE id=? AND booking_id=?")) {
                    ps.setLong(1, newId);
                    ps.setLong(2, detailId);
                    ps.setLong(3, bookingId);
                    if (ps.executeUpdate() == 0) {
                        throw new SQLException("Phòng cần tách không thuộc đơn này.");
                    }
                }
                c.commit();
                return newId;
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    @Override
    public void merge(long sourceId, long targetId) throws SQLException {
        if (sourceId == targetId) throw new SQLException("Chọn hai đơn khác nhau để ghép.");
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                BookMeta src = lockMeta(c, sourceId);
                BookMeta dst = lockMeta(c, targetId);
                if (src.customerId() != dst.customerId()) {
                    throw new SQLException("Chỉ ghép được các đơn của cùng một khách hàng.");
                }
                if (!src.checkIn().equals(dst.checkIn()) || !src.checkOut().equals(dst.checkOut())) {
                    throw new SQLException("Chỉ ghép được các đơn có cùng khoảng ngày lưu trú.");
                }
                requireSplittable(src.status());
                requireSplittable(dst.status());
                if (hasPaid(c, sourceId) || hasPaid(c, targetId)) {
                    throw new SQLException("Đơn đã phát sinh thanh toán, không thể ghép.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT COUNT(*) FROM booking_details a JOIN booking_details b ON a.room_id=b.room_id " +
                                "WHERE a.booking_id=? AND b.booking_id=?")) {
                    ps.setLong(1, sourceId);
                    ps.setLong(2, targetId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) throw new SQLException("Hai đơn có phòng trùng nhau, không thể ghép.");
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE booking_details SET booking_id=? WHERE booking_id=?")) {
                    ps.setLong(1, targetId);
                    ps.setLong(2, sourceId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM bookings WHERE id=?")) {
                    ps.setLong(1, sourceId);
                    if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy đơn cần ghép.");
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private record BookMeta(long customerId, LocalDate checkIn, LocalDate checkOut, String status) {}

    private BookMeta lockMeta(Connection c, long bookingId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT customer_id,check_in,check_out,status FROM bookings WHERE id=? FOR UPDATE")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy đặt phòng #" + bookingId);
                return new BookMeta(rs.getLong("customer_id"), rs.getDate("check_in").toLocalDate(),
                        rs.getDate("check_out").toLocalDate(), rs.getString("status"));
            }
        }
    }

    private void requireSplittable(String status) throws SQLException {
        if (!"PENDING".equals(status) && !"CONFIRMED".equals(status)) {
            throw new SQLException("Chỉ thực hiện được với đơn ở trạng thái Chờ xử lý hoặc Đã xác nhận.");
        }
    }

    private boolean hasPaid(Connection c, long bookingId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE booking_id=? AND status='PAID'")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private int detailCount(Connection c, long bookingId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM booking_details WHERE booking_id=?")) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    public void cancel(long id) throws SQLException {
        transition(id, "'PENDING','CONFIRMED'", "CANCELLED", "Không thể hủy đơn này.", null);
    }

    public void checkIn(long id) throws SQLException {
        transition(id, "'CONFIRMED'", "CHECKED_IN", "Chỉ nhận phòng các đơn đã xác nhận.", "OCCUPIED");
    }

    public void checkOut(long id) throws SQLException {
        transition(id, "'CHECKED_IN'", "CHECKED_OUT", "Chỉ trả phòng khi khách đã nhận phòng.", "AVAILABLE");
    }

    /**
     * Đổi trạng thái đơn và (tuỳ chọn) đồng bộ trạng thái phòng trong cùng một transaction,
     * để phòng không còn hiển thị "Trống" khi khách đang ở.
     */
    private void transition(long id, String fromCsv, String toStatus, String errorMessage, String roomStatus)
            throws SQLException {
        String sql = "UPDATE bookings SET status=? WHERE id=? AND status IN (" + fromCsv + ")";
        try (Connection c = DatabaseConnection.getInstance().getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, toStatus);
                    ps.setLong(2, id);
                    if (ps.executeUpdate() == 0) throw new SQLException(errorMessage);
                }
                if (roomStatus != null) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE rooms r JOIN booking_details d ON d.room_id=r.id SET r.status=? WHERE d.booking_id=?")) {
                        ps.setString(1, roomStatus);
                        ps.setLong(2, id);
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException se) throw se;
                if (e instanceof RuntimeException re) throw re;
                throw new SQLException(e);
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        BigDecimal total = rs.getBigDecimal("total");
        BigDecimal paid = rs.getBigDecimal("paid");
        String paymentStatus;
        if (paid.compareTo(BigDecimal.ZERO) <= 0) paymentStatus = "UNPAID";
        else if (paid.compareTo(total) >= 0) paymentStatus = "PAID";
        else paymentStatus = "PARTIAL";
        return new Booking(
                rs.getLong("id"),
                rs.getString("booking_code"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getDate("check_in").toLocalDate(),
                rs.getDate("check_out").toLocalDate(),
                rs.getString("status"),
                total,
                paymentStatus
        );
    }
}

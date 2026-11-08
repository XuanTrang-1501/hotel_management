package com.hotel.repository;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface PaymentRepository {
    BigDecimal totalPaid(long bookingId) throws SQLException;

    void insert(long bookingId, BigDecimal amount, String method) throws SQLException;

    BigDecimal totalRevenue() throws SQLException;
}

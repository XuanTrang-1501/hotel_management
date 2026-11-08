package com.hotel.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.hotel.model.Invoice;

public interface InvoiceRepository {
    Invoice findByBooking(long bookingId) throws SQLException;

    Invoice createIfAbsent(long bookingId, BigDecimal total) throws SQLException;

    List<Invoice> findAll() throws SQLException;
}

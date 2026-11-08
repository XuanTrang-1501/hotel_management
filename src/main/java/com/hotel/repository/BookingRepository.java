package com.hotel.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.hotel.model.Booking;
import com.hotel.model.BookingDetail;
import com.hotel.patterns.builder.BookingRequest;
import com.hotel.patterns.strategy.PricingStrategy;


public interface BookingRepository {
    List<Booking> findAll() throws SQLException;

    Booking findById(long id) throws SQLException;

    BigDecimal basePriceOf(long roomId) throws SQLException;

    void create(BookingRequest request, PricingStrategy pricing) throws SQLException;

    List<BookingDetail> findDetails(long bookingId) throws SQLException;

    long splitRoom(long bookingId, long detailId) throws SQLException;

    void merge(long sourceId, long targetId) throws SQLException;

    void cancel(long id) throws SQLException;

    void checkIn(long id) throws SQLException;

    void checkOut(long id) throws SQLException;
}

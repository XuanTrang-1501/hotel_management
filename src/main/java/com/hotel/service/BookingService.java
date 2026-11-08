package com.hotel.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.hotel.repository.BookingRepository;
import com.hotel.model.Booking;
import com.hotel.model.BookingDetail;
import com.hotel.model.Invoice;
import com.hotel.patterns.builder.BookingRequest;
import com.hotel.patterns.facade.BookingFacade;
import com.hotel.service.PaymentService.SettleResult;

public class BookingService {
    private final BookingRepository dao;
    private final BookingFacade facade;

    public BookingService(BookingRepository dao, BookingFacade facade) {
        this.dao = dao;
        this.facade = facade;
    }

    public List<Booking> getBookings() throws SQLException {
        return dao.findAll();
    }

    public Booking findById(long id) throws SQLException {
        return dao.findById(id);
    }

    public void book(long customer, long room, LocalDate in, LocalDate out, long user) throws SQLException {
        book(customer, List.of(room), in, out, user, null);
    }

    public void book(long customer, List<Long> rooms, LocalDate in, LocalDate out, long user, String promoCode)
            throws SQLException {
        BookingRequest request = new BookingRequest.Builder()
                .customerId(customer)
                .roomIds(rooms)
                .userId(user)
                .checkIn(in)
                .checkOut(out)
                .promoCode(promoCode)
                .build();
        facade.book(request);
    }

    public List<BookingDetail> details(long bookingId) throws SQLException {
        return dao.findDetails(bookingId);
    }
    
    public long splitRoom(long bookingId, long detailId) throws SQLException {
        return facade.splitRoom(bookingId, detailId);
    }

    public void merge(long sourceId, long targetId) throws SQLException {
        facade.merge(sourceId, targetId);
    }

    public void checkIn(long id) throws SQLException {
        facade.checkIn(id);
    }

    public void checkOut(long id) throws SQLException {
        facade.checkOut(id);
    }

    public void cancel(long id) throws SQLException {
        facade.cancel(id);
    }

    public Invoice pay(long id, String method) throws SQLException {
        return facade.pay(id, method);
    }

    public SettleResult pay(long id, String method, BigDecimal amount) throws SQLException {
        return facade.pay(id, method, amount);
    }

    public BigDecimal remaining(long id) throws SQLException {
        return facade.remaining(id);
    }

    public BigDecimal estimate(long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        return facade.estimate(roomId, checkIn, checkOut);
    }

    public BigDecimal estimate(List<Long> roomIds, LocalDate checkIn, LocalDate checkOut, String promoCode)
            throws SQLException {
        return facade.estimate(roomIds, checkIn, checkOut, promoCode);
    }

    public Invoice invoice(long id) throws SQLException {
        return facade.invoice(id);
    }
}

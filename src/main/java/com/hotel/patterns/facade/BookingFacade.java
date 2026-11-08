package com.hotel.patterns.facade;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.hotel.repository.BookingRepository;
import com.hotel.model.Invoice;
import com.hotel.patterns.builder.BookingRequest;
import com.hotel.patterns.observer.BookingNotifier;
import com.hotel.patterns.strategy.PricingSelector;
import com.hotel.patterns.strategy.PricingStrategy;
import com.hotel.service.PaymentService;

public class BookingFacade {
    private final BookingRepository bookingDAO;
    private final PaymentService paymentService;
    private final BookingNotifier notifier;

    public BookingFacade(BookingRepository bookingDAO, PaymentService paymentService, BookingNotifier notifier) {
        this.bookingDAO = bookingDAO;
        this.paymentService = paymentService;
        this.notifier = notifier;
    }

    public void book(BookingRequest request) throws SQLException {
        PricingStrategy pricing = PricingSelector.of(request.checkIn(), request.checkOut(), request.promoCode());
        bookingDAO.create(request, pricing);
        notifier.notifyAllObservers("Đã tạo đặt phòng " + request.roomIds().size() + " phòng cho khách #"
                + request.customerId() + ". Áp dụng " + pricing.name() + ".");
    }

    public long splitRoom(long bookingId, long detailId) throws SQLException {
        long newId = bookingDAO.splitRoom(bookingId, detailId);
        notifier.notifyAllObservers("Đã tách một phòng từ đơn #" + bookingId + " sang đơn mới #" + newId + ".");
        return newId;
    }

    public void merge(long sourceId, long targetId) throws SQLException {
        bookingDAO.merge(sourceId, targetId);
        notifier.notifyAllObservers("Đã gộp đơn #" + sourceId + " vào đơn #" + targetId + ".");
    }

    public void checkIn(long bookingId) throws SQLException {
        bookingDAO.checkIn(bookingId);
        notifier.notifyAllObservers("Khách đã nhận phòng cho đơn #" + bookingId + ".");
    }

    public void checkOut(long bookingId) throws SQLException {
        bookingDAO.checkOut(bookingId);
        notifier.notifyAllObservers("Khách đã trả phòng cho đơn #" + bookingId + ".");
    }

    public void cancel(long bookingId) throws SQLException {
        if (paymentService.hasPayments(bookingId)) {
            throw new IllegalStateException("Đơn đã có thanh toán. Vui lòng hoàn tiền trước khi hủy.");
        }
        bookingDAO.cancel(bookingId);
        notifier.notifyAllObservers("Đã hủy đặt phòng #" + bookingId + ".");
    }

    public Invoice pay(long bookingId, String method) throws SQLException {
        Invoice invoice = paymentService.settle(bookingId, method);
        notifier.notifyAllObservers("Thanh toán thành công. Hóa đơn " + invoice.invoiceCode() + ".");
        return invoice;
    }

    public PaymentService.SettleResult pay(long bookingId, String method, BigDecimal amount) throws SQLException {
        PaymentService.SettleResult result = paymentService.settle(bookingId, method, amount);
        if (result.fullyPaid()) {
            notifier.notifyAllObservers("Đã thu đủ. Hóa đơn " + result.invoice().invoiceCode() + ".");
        } else {
            notifier.notifyAllObservers("Đã ghi nhận thanh toán. Còn lại " + result.remaining() + ".");
        }
        return result;
    }

    public BigDecimal remaining(long bookingId) throws SQLException {
        return paymentService.remaining(bookingId);
    }

    public BigDecimal estimate(long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        return estimate(List.of(roomId), checkIn, checkOut, null);
    }

    public BigDecimal estimate(List<Long> roomIds, LocalDate checkIn, LocalDate checkOut, String promoCode)
            throws SQLException {
        PricingStrategy pricing = PricingSelector.of(checkIn, checkOut, promoCode);
        BigDecimal total = BigDecimal.ZERO;
        for (long roomId : roomIds) {
            BigDecimal base = bookingDAO.basePriceOf(roomId);
            total = total.add(pricing.calculate(base, checkIn, checkOut));
        }
        return total;
    }

    public Invoice invoice(long bookingId) throws SQLException {
        return paymentService.findInvoice(bookingId);
    }
}

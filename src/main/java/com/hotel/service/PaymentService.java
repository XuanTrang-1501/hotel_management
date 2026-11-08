package com.hotel.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.hotel.repository.BookingRepository;
import com.hotel.repository.InvoiceRepository;
import com.hotel.repository.PaymentRepository;
import com.hotel.model.Booking;
import com.hotel.model.Invoice;
import com.hotel.patterns.factory.Payment;
import com.hotel.patterns.factory.PaymentFactory;

public class PaymentService {
    private final PaymentRepository paymentDAO;
    private final InvoiceRepository invoiceDAO;
    private final BookingRepository bookingDAO;

    public PaymentService(PaymentRepository paymentDAO, InvoiceRepository invoiceDAO, BookingRepository bookingDAO) {
        this.paymentDAO = paymentDAO;
        this.invoiceDAO = invoiceDAO;
        this.bookingDAO = bookingDAO;
    }

    public Invoice settle(long bookingId, String method) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        SettleResult result = settle(bookingId, method, booking.total().subtract(paymentDAO.totalPaid(bookingId)));
        return result.invoice();
    }

    public SettleResult settle(long bookingId, String method, BigDecimal amount) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        if ("CANCELLED".equalsIgnoreCase(booking.status())) {
            throw new IllegalArgumentException("Không thể thanh toán đơn đã hủy.");
        }
        BigDecimal paidBefore = paymentDAO.totalPaid(bookingId);
        BigDecimal due = booking.total().subtract(paidBefore);
        if (due.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Đơn này đã thanh toán đủ.");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Nhập số tiền thanh toán.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }
        if (amount.compareTo(due) > 0) {
            throw new IllegalArgumentException("Số tiền vượt quá số còn lại (" + due + ").");
        }
        String dbMethod = normalizeMethod(method);
        Payment gateway = PaymentFactory.create(dbMethod);
        gateway.pay(amount);
        paymentDAO.insert(bookingId, amount, dbMethod);
        BigDecimal paid = paidBefore.add(amount);
        BigDecimal remaining = booking.total().subtract(paid).max(BigDecimal.ZERO);
        boolean fullyPaid = remaining.compareTo(BigDecimal.ZERO) <= 0;
        Invoice invoice = fullyPaid ? invoiceDAO.createIfAbsent(bookingId, booking.total()) : null;
        return new SettleResult(invoice, paid, remaining, fullyPaid);
    }

    public BigDecimal remaining(long bookingId) throws SQLException {
        Booking booking = bookingDAO.findById(bookingId);
        return booking.total().subtract(paymentDAO.totalPaid(bookingId)).max(BigDecimal.ZERO);
    }

    public record SettleResult(Invoice invoice, BigDecimal paid, BigDecimal remaining, boolean fullyPaid) {}

    public Invoice findInvoice(long bookingId) throws SQLException {
        Invoice invoice = invoiceDAO.findByBooking(bookingId);
        if (invoice == null) throw new IllegalArgumentException("Đơn này chưa có hóa đơn. Hãy thanh toán trước.");
        return invoice;
    }

    public boolean hasPayments(long bookingId) throws SQLException {
        return paymentDAO.totalPaid(bookingId).compareTo(BigDecimal.ZERO) > 0;
    }

    public List<Invoice> getInvoices() throws SQLException {
        return invoiceDAO.findAll();
    }

    public BigDecimal getRevenue() throws SQLException {
        return paymentDAO.totalRevenue();
    }

    private String normalizeMethod(String method) {
        if (method == null) throw new IllegalArgumentException("Chưa chọn phương thức thanh toán.");
        return switch (method.toUpperCase()) {
            case "CASH", "TIỀN MẶT", "TIEN MAT" -> "CASH";
            case "CARD", "THẺ", "THE" -> "CARD";
            case "BANK", "BANK_TRANSFER", "CHUYỂN KHOẢN", "CHUYEN KHOAN" -> "BANK_TRANSFER";
            default -> method.toUpperCase();
        };
    }
}

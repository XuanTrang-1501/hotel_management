package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Invoice(long id, String invoiceCode, String bookingCode, String customerName,
                      BigDecimal totalAmount, LocalDateTime issuedAt) {}

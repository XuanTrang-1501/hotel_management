package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Booking(long id, String bookingCode, String customerName, String phone,
                      LocalDate checkIn, LocalDate checkOut, String status, BigDecimal total,
                      String paymentStatus) {}

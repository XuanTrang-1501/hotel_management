package com.hotel.model;

import java.math.BigDecimal;

public record BookingDetail(long id, long bookingId, long roomId, String roomNumber,
                            String typeName, BigDecimal pricePerNight, int nights, BigDecimal lineTotal) {}

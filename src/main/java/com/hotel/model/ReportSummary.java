package com.hotel.model;

import java.math.BigDecimal;

public record ReportSummary(BigDecimal totalRevenue, int bookingCount,
                            long bookedRoomNights, long totalRoomNights) {

    /** Công suất phòng (0.0 – 1.0). */
    public double occupancyRate() {
        return totalRoomNights <= 0 ? 0d : (double) bookedRoomNights / totalRoomNights;
    }
}

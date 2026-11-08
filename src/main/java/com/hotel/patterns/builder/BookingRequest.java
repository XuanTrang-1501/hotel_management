package com.hotel.patterns.builder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class BookingRequest {
    private final long customerId;
    private final List<Long> roomIds;
    private final long userId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final String promoCode;

    private BookingRequest(Builder builder) {
        this.customerId = builder.customerId;
        this.roomIds = List.copyOf(builder.roomIds);
        this.userId = builder.userId;
        this.checkIn = builder.checkIn;
        this.checkOut = builder.checkOut;
        this.promoCode = builder.promoCode;
    }

    public long customerId() { return customerId; }
    public List<Long> roomIds() { return roomIds; }
    public long roomId() { return roomIds.isEmpty() ? 0 : roomIds.get(0); }
    public long userId() { return userId; }
    public LocalDate checkIn() { return checkIn; }
    public LocalDate checkOut() { return checkOut; }
    public String promoCode() { return promoCode; }

    public static class Builder {
        private long customerId;
        private final Set<Long> roomIds = new LinkedHashSet<>();
        private long userId;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private String promoCode;

        public Builder customerId(long customerId) { this.customerId = customerId; return this; }

        public Builder roomId(long roomId) { this.roomIds.add(roomId); return this; }

        public Builder roomIds(Iterable<Long> roomIds) {
            if (roomIds != null) for (Long r : roomIds) if (r != null) this.roomIds.add(r);
            return this;
        }

        public Builder userId(long userId) { this.userId = userId; return this; }
        public Builder checkIn(LocalDate checkIn) { this.checkIn = checkIn; return this; }
        public Builder checkOut(LocalDate checkOut) { this.checkOut = checkOut; return this; }
        public Builder promoCode(String promoCode) { this.promoCode = promoCode; return this; }

        public BookingRequest build() {
            if (customerId <= 0) {
                throw new IllegalArgumentException("Khách hàng là bắt buộc.");
            }
            List<Long> cleaned = new ArrayList<>();
            for (Long r : roomIds) {
                if (r == null || r <= 0) throw new IllegalArgumentException("Phòng không hợp lệ.");
                cleaned.add(r);
            }
            if (cleaned.isEmpty()) {
                throw new IllegalArgumentException("Cần chọn ít nhất một phòng.");
            }
            if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
                throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
            }
            if (checkIn.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Ngày nhận phòng không được ở quá khứ.");
            }
            return new BookingRequest(this);
        }
    }
}

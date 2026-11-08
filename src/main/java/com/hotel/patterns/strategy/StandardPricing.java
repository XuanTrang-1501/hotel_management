package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class StandardPricing implements PricingStrategy {
    @Override
    public String name() {
        return "Giá tiêu chuẩn";
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) throw new IllegalArgumentException("Số đêm phải lớn hơn 0.");
        return basePrice.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }
}

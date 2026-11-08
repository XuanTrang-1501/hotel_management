package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class WeekendPricing implements PricingStrategy {
    private static final BigDecimal WEEKEND_RATE = new BigDecimal("1.10");

    @Override
    public String name() {
        return "Giá cuối tuần (+10%)";
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate day = checkIn; day.isBefore(checkOut); day = day.plusDays(1)) {
            DayOfWeek week = day.getDayOfWeek();
            BigDecimal factor = (week == DayOfWeek.SATURDAY || week == DayOfWeek.SUNDAY)
                    ? WEEKEND_RATE : BigDecimal.ONE;
            total = total.add(basePrice.multiply(factor));
        }
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số đêm phải lớn hơn 0.");
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }
}

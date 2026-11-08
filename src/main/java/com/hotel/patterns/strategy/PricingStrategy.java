package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingStrategy {
    String name();
    BigDecimal calculate(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut);
}

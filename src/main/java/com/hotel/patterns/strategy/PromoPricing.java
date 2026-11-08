package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class PromoPricing implements PricingStrategy {

    private final PricingStrategy delegate;
    private final String code;
    private final BigDecimal discount;

    public PromoPricing(PricingStrategy delegate, String code, BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) < 0
                || discount.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Tỉ lệ giảm giá không hợp lệ.");
        }
        this.delegate = delegate;
        this.code = code == null ? "" : code.trim().toUpperCase();
        this.discount = discount;
    }

    @Override
    public String name() {
        return delegate.name() + " + KM " + code + " (-" + discount.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%)";
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal amount = delegate.calculate(basePrice, checkIn, checkOut);
        return amount.multiply(BigDecimal.ONE.subtract(discount)).setScale(2, RoundingMode.HALF_UP);
    }
}

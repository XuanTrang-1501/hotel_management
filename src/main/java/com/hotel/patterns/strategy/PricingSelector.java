package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

public final class PricingSelector {
    private PricingSelector() {}

    public static PricingStrategy of(LocalDate checkIn, LocalDate checkOut) {
        return of(checkIn, checkOut, null);
    }

    public static PricingStrategy of(LocalDate checkIn, LocalDate checkOut, String promoCode) {
        PricingStrategy base = baseStrategy(checkIn, checkOut);
        BigDecimal discount = PromoCatalog.discountOf(promoCode);
        if (discount != null) return new PromoPricing(base, promoCode, discount);
        return base;
    }

    private static PricingStrategy baseStrategy(LocalDate checkIn, LocalDate checkOut) {
        if (SeasonPricing.highSeason(checkIn, checkOut)) return new SeasonPricing();
        for (LocalDate day = checkIn; day.isBefore(checkOut); day = day.plusDays(1)) {
            DayOfWeek week = day.getDayOfWeek();
            if (week == DayOfWeek.SATURDAY || week == DayOfWeek.SUNDAY) {
                return new WeekendPricing();
            }
        }
        return new StandardPricing();
    }
}

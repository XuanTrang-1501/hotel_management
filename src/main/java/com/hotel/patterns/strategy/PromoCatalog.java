package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public final class PromoCatalog {

    private static final Map<String, BigDecimal> DISCOUNTS = Map.of(
            "WELCOME5", new BigDecimal("0.05"),
            "SUMMER10", new BigDecimal("0.10"),
            "GROUP15", new BigDecimal("0.15")
    );

    private PromoCatalog() {}

    public static BigDecimal discountOf(String code) {
        if (code == null || code.isBlank()) return null;
        return DISCOUNTS.get(code.trim().toUpperCase());
    }

    public static Set<String> codes() {
        return DISCOUNTS.keySet();
    }
}

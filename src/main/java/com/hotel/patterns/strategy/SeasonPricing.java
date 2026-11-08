package com.hotel.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;

public class SeasonPricing implements PricingStrategy {

    private record Season(MonthDay from, MonthDay to, BigDecimal rate, String label) {}

    private static final List<Season> SEASONS = List.of(
            new Season(MonthDay.of(1, 20), MonthDay.of(2, 5), new BigDecimal("1.30"), "Tết"),
            new Season(MonthDay.of(4, 28), MonthDay.of(5, 3), new BigDecimal("1.20"), "Lễ 30/4 – 1/5"),
            new Season(MonthDay.of(6, 1), MonthDay.of(8, 31), new BigDecimal("1.25"), "Hè")
    );

    @Override
    public String name() {
        return "Giá cao điểm theo mùa";
    }

    @Override
    public BigDecimal calculate(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal total = BigDecimal.ZERO;
        long nights = 0;
        for (LocalDate day = checkIn; day.isBefore(checkOut); day = day.plusDays(1)) {
            nights++;
            total = total.add(basePrice.multiply(rateOf(day)));
        }
        if (nights <= 0) throw new IllegalArgumentException("Số đêm phải lớn hơn 0.");
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /** Hệ số phụ thu của một ngày (1.0 nếu không thuộc mùa cao điểm). */
    public static BigDecimal rateOf(LocalDate day) {
        MonthDay md = MonthDay.from(day);
        for (Season s : SEASONS) {
            if (!md.isBefore(s.from()) && !md.isAfter(s.to())) return s.rate();
        }
        return BigDecimal.ONE;
    }

    /** Có ít nhất một đêm nằm trong mùa cao điểm hay không. */
    public static boolean highSeason(LocalDate checkIn, LocalDate checkOut) {
        for (LocalDate day = checkIn; day.isBefore(checkOut); day = day.plusDays(1)) {
            if (rateOf(day).compareTo(BigDecimal.ONE) > 0) return true;
        }
        return false;
    }
}

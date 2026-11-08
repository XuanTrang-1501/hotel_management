package com.hotel.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.hotel.repository.ReportRepository;
import com.hotel.model.ChartPoint;
import com.hotel.model.ReportSummary;


public class ReportService {
    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");
    private static final int MAX_DAYS = 120;

    private final ReportRepository dao;

    public ReportService(ReportRepository dao) {
        this.dao = dao;
    }

    public ReportSummary summary(LocalDate from, LocalDate to) throws SQLException {
        validate(from, to);
        return dao.summary(from, to);
    }

    public List<ChartPoint> revenueByDay(LocalDate from, LocalDate to) throws SQLException {
        validate(from, to);
        Map<LocalDate, BigDecimal> paid = new LinkedHashMap<>();
        for (ChartPoint p : dao.revenueByDay(from, to)) {
            paid.put(LocalDate.parse(p.label()), p.value());
        }
        List<ChartPoint> result = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            result.add(new ChartPoint(d.format(DAY_LABEL), paid.getOrDefault(d, BigDecimal.ZERO)));
        }
        return result;
    }

    public List<ChartPoint> revenueByMonth(LocalDate from, LocalDate to) throws SQLException {
        validate(from, to);
        return dao.revenueByMonth(from, to);
    }

    public List<ChartPoint> revenueByRoomType() throws SQLException {
        return dao.revenueByRoomType();
    }

    private void validate(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new IllegalArgumentException("Chọn khoảng ngày báo cáo.");
        if (to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu.");
        if (ChronoUnit.DAYS.between(from, to) > MAX_DAYS) {
            throw new IllegalArgumentException("Khoảng báo cáo tối đa " + MAX_DAYS + " ngày.");
        }
    }
}

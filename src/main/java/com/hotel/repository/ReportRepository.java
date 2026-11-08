package com.hotel.repository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.hotel.model.ChartPoint;
import com.hotel.model.ReportSummary;


public interface ReportRepository {
    List<ChartPoint> revenueByDay(LocalDate from, LocalDate to) throws SQLException;

    List<ChartPoint> revenueByMonth(LocalDate from, LocalDate to) throws SQLException;

    List<ChartPoint> revenueByRoomType() throws SQLException;

    ReportSummary summary(LocalDate from, LocalDate to) throws SQLException;
}

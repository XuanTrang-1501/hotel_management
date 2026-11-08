package com.hotel.dao;

import java.sql.SQLException;

final class SqlErrors {
    private SqlErrors() {}

    static SQLException friendly(SQLException e) {
        if (!isIntegrityViolation(e)) return e;
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (message.contains("uk_customer_phone") || (message.contains("duplicate") && message.contains("phone"))) {
            return new SQLException("Số điện thoại này đã được dùng cho khách hàng khác.", e);
        }
        if (message.contains("room_number")) {
            return new SQLException("Số phòng này đã tồn tại.", e);
        }
        if (message.contains("type_name")) {
            return new SQLException("Tên loại phòng này đã tồn tại.", e);
        }
        if (message.contains("parent row")) {
            return new SQLException("Không thể xoá vì dữ liệu đang được tham chiếu.", e);
        }
        if (message.contains("foreign key") || message.contains("child row")) {
            return new SQLException("Dữ liệu tham chiếu không hợp lệ hoặc đang được sử dụng.", e);
        }
        return new SQLException("Dữ liệu không hợp lệ hoặc vi phạm ràng buộc.", e);
    }

    private static boolean isIntegrityViolation(SQLException e) {
        int code = e.getErrorCode();
        String state = e.getSQLState();
        return code == 1062 || code == 1451 || code == 1452 || (state != null && state.startsWith("23"));
    }
}

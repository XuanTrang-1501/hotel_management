package com.hotel.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.hotel.repository.RoomRepository;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

public class RoomService {
    private final RoomRepository dao;

    public RoomService(RoomRepository dao) {
        this.dao = dao;
    }

    public List<Room> getRooms() throws SQLException {
        return dao.findAll();
    }

    public List<RoomType> getTypes() throws SQLException {
        return dao.findTypes();
    }

    public List<RoomType> getAllTypes() throws SQLException {
        return dao.findAllTypes();
    }

    public void add(String number, long typeId, int floor) throws SQLException {
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Số phòng là bắt buộc.");
        if (typeId <= 0) throw new IllegalArgumentException("Chọn loại phòng.");
        if (floor <= 0) throw new IllegalArgumentException("Tầng phải lớn hơn 0.");
        dao.add(number.trim(), typeId, floor);
    }

    public void update(long id, String number, long typeId, int floor) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Chưa chọn phòng.");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Số phòng là bắt buộc.");
        if (typeId <= 0) throw new IllegalArgumentException("Chọn loại phòng.");
        if (floor <= 0) throw new IllegalArgumentException("Tầng phải lớn hơn 0.");
        dao.update(id, number.trim(), typeId, floor);
    }

    public void setStatus(long id, String status) throws SQLException {
        if (!List.of("AVAILABLE", "MAINTENANCE", "INACTIVE").contains(status)) {
            throw new IllegalArgumentException("Trạng thái phòng không hợp lệ.");
        }
        dao.updateStatus(id, status);
    }

    public void addType(String typeName, String description, BigDecimal pricePerNight, int capacity) throws SQLException {
        validateType(typeName, pricePerNight, capacity);
        dao.addType(typeName.trim(), blankToNull(description), pricePerNight, capacity);
    }

    public void updateType(long id, String typeName, String description, BigDecimal pricePerNight, int capacity)
            throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Chưa chọn loại phòng.");
        validateType(typeName, pricePerNight, capacity);
        dao.updateType(id, typeName.trim(), blankToNull(description), pricePerNight, capacity);
    }

    public void updateTypePrice(long typeId, BigDecimal pricePerNight) throws SQLException {
        if (typeId <= 0) throw new IllegalArgumentException("Chưa chọn loại phòng.");
        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá mỗi đêm phải lớn hơn 0.");
        }
        dao.updateTypePrice(typeId, pricePerNight);
    }

    public void setTypeActive(long id, boolean active) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Chưa chọn loại phòng.");
        dao.setTypeActive(id, active);
    }

    private void validateType(String typeName, BigDecimal pricePerNight, int capacity) {
        if (typeName == null || typeName.isBlank()) throw new IllegalArgumentException("Tên loại phòng là bắt buộc.");
        if (pricePerNight == null || pricePerNight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá mỗi đêm phải lớn hơn 0.");
        }
        if (capacity <= 0) throw new IllegalArgumentException("Sức chứa phải lớn hơn 0.");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

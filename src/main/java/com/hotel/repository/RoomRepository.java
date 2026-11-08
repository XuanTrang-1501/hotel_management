package com.hotel.repository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.hotel.model.Room;
import com.hotel.model.RoomType;

public interface RoomRepository {
    List<Room> findAll() throws SQLException;

    List<RoomType> findTypes() throws SQLException;

    List<RoomType> findAllTypes() throws SQLException;

    void add(String number, long typeId, int floor) throws SQLException;

    void update(long id, String number, long typeId, int floor) throws SQLException;

    void updateStatus(long id, String status) throws SQLException;

    void addType(String typeName, String description, BigDecimal pricePerNight, int capacity) throws SQLException;

    void updateType(long id, String typeName, String description, BigDecimal pricePerNight, int capacity) throws SQLException;

    void updateTypePrice(long typeId, BigDecimal pricePerNight) throws SQLException;

    void setTypeActive(long id, boolean active) throws SQLException;
}

package com.hotel.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotel.config.DatabaseConnection;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.repository.RoomRepository;

public class RoomDAO implements RoomRepository {
    private static final String TYPE_SELECT =
            "SELECT id,type_name,description,price_per_night,capacity,active FROM room_types ";

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT r.id,r.room_number,t.type_name,t.price_per_night,r.floor_number,r.status " +
                "FROM rooms r JOIN room_types t ON r.room_type_id=t.id ORDER BY r.room_number";
        List<Room> result = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(new Room(rs.getLong(1), rs.getString(2), rs.getString(3),
                    rs.getBigDecimal(4), rs.getInt(5), rs.getString(6)));
        }
        return result;
    }

    public List<RoomType> findTypes() throws SQLException {
        return queryTypes(TYPE_SELECT + "WHERE active=1 ORDER BY type_name");
    }

    public List<RoomType> findAllTypes() throws SQLException {
        return queryTypes(TYPE_SELECT + "ORDER BY active DESC, type_name");
    }

    private List<RoomType> queryTypes(String sql) throws SQLException {
        List<RoomType> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new RoomType(rs.getLong(1), rs.getString(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getInt(5), rs.getBoolean(6)));
            }
        }
        return list;
    }

    public void add(String number, long typeId, int floor) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO rooms(room_number,room_type_id,floor_number) VALUES(?,?,?)")) {
            ps.setString(1, number);
            ps.setLong(2, typeId);
            ps.setInt(3, floor);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void update(long id, String number, long typeId, int floor) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE rooms SET room_number=?, room_type_id=?, floor_number=? WHERE id=?")) {
            ps.setString(1, number);
            ps.setLong(2, typeId);
            ps.setInt(3, floor);
            ps.setLong(4, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy phòng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void updateStatus(long id, String status) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE rooms SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setLong(2, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy phòng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void addType(String typeName, String description, BigDecimal pricePerNight, int capacity) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO room_types(type_name,description,price_per_night,capacity) VALUES(?,?,?,?)")) {
            ps.setString(1, typeName);
            ps.setString(2, description);
            ps.setBigDecimal(3, pricePerNight);
            ps.setInt(4, capacity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void updateType(long id, String typeName, String description, BigDecimal pricePerNight, int capacity)
            throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE room_types SET type_name=?, description=?, price_per_night=?, capacity=? WHERE id=?")) {
            ps.setString(1, typeName);
            ps.setString(2, description);
            ps.setBigDecimal(3, pricePerNight);
            ps.setInt(4, capacity);
            ps.setLong(5, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy loại phòng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void updateTypePrice(long typeId, BigDecimal pricePerNight) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE room_types SET price_per_night=? WHERE id=? AND active=1")) {
            ps.setBigDecimal(1, pricePerNight);
            ps.setLong(2, typeId);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy loại phòng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void setTypeActive(long id, boolean active) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE room_types SET active=? WHERE id=?")) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy loại phòng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }
}

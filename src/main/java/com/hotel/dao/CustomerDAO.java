package com.hotel.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotel.config.DatabaseConnection;
import com.hotel.model.Customer;
import com.hotel.repository.CustomerRepository;

public class CustomerDAO implements CustomerRepository {
    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id,full_name,phone,email,identity_number,address FROM customers ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Customer(rs.getLong(1), rs.getString(2), rs.getString(3),
                    rs.getString(4), rs.getString(5), rs.getString(6)));
        }
        return list;
    }

    public void add(String name, String phone, String email, String identity, String address) throws SQLException {
        String sql = "INSERT INTO customers(full_name,phone,email,identity_number,address) VALUES(?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, emptyToNull(email));
            ps.setString(4, emptyToNull(identity));
            ps.setString(5, emptyToNull(address));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    public void update(long id, String name, String phone, String email, String identity, String address) throws SQLException {
        String sql = "UPDATE customers SET full_name=?,phone=?,email=?,identity_number=?,address=? WHERE id=?";
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, phone);
            ps.setString(3, emptyToNull(email));
            ps.setString(4, emptyToNull(identity));
            ps.setString(5, emptyToNull(address));
            ps.setLong(6, id);
            if (ps.executeUpdate() == 0) throw new SQLException("Không tìm thấy khách hàng.");
        } catch (SQLException e) {
            throw SqlErrors.friendly(e);
        }
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

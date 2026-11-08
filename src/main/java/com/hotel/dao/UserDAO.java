package com.hotel.dao;

import com.hotel.config.DatabaseConnection;
import com.hotel.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO implements UserRepository {
    private static final String FIND_SQL = """
            SELECT u.id,u.username,u.password_hash,u.full_name,r.role_name
            FROM users u JOIN roles r ON r.id=u.role_id
            WHERE u.username=? AND u.active=1
            """;

    @Override
    public Account findByUsername(String username) throws SQLException {
        try (Connection c = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(FIND_SQL)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Account(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("full_name"),
                        rs.getString("role_name"));
            }
        }
    }
}

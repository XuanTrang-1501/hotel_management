package com.hotel.repository;

import java.sql.SQLException;

public interface UserRepository {
    record Account(long id, String username, String passwordHash, String fullName, String role) {}

    Account findByUsername(String username) throws SQLException;
}

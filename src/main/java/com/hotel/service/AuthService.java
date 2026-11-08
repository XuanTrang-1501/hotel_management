package com.hotel.service;

import com.hotel.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;


public class AuthService {
    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public record Session(long id, String username, String fullName, String role) {}

    public Session login(String username, String password) throws SQLException {
        UserRepository.Account account = repository.findByUsername(username);
        if (account == null) return null;
        // Chỉ so khớp hash BCrypt; không lưu mật khẩu dạng rõ trong database.
        if (!BCrypt.checkpw(password, account.passwordHash())) return null;
        return new Session(account.id(), account.username(), account.fullName(), account.role());
    }
}

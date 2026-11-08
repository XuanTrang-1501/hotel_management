package com.hotel.service;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.hotel.repository.UserRepository;


public class AuthService {
    private final UserRepository repository;

    public AuthService(UserRepository repository) {
        this.repository = repository;
    }

    public record Session(long id, String username, String fullName, String role) {}

    public Session login(String username, String password) throws SQLException {
        UserRepository.Account account = repository.findByUsername(username);
        if (account == null) return null;
        if (!BCrypt.checkpw(password, account.passwordHash())) return null;
        return new Session(account.id(), account.username(), account.fullName(), account.role());
    }
}

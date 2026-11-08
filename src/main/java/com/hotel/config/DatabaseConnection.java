package com.hotel.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {
    private static volatile DatabaseConnection instance;
    private final String url = System.getenv().getOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/hotel_management?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true");
    private final String user = System.getenv().getOrDefault("DB_USER", "root");
    private final String password = System.getenv().getOrDefault("DB_PASSWORD", "150104");

    private DatabaseConnection() {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) { throw new IllegalStateException("Không tìm thấy MySQL JDBC Driver", e); }
    }
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) instance = new DatabaseConnection();
            }
        }
        return instance;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}

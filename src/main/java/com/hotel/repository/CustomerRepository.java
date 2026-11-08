package com.hotel.repository;

import com.hotel.model.Customer;

import java.sql.SQLException;
import java.util.List;

public interface CustomerRepository {
    List<Customer> findAll() throws SQLException;

    void add(String name, String phone, String email, String identity, String address) throws SQLException;

    void update(long id, String name, String phone, String email, String identity, String address) throws SQLException;
}

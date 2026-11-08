package com.hotel.service;

import com.hotel.repository.CustomerRepository;
import com.hotel.model.Customer;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {
    private final CustomerRepository dao;

    public CustomerService(CustomerRepository dao) {
        this.dao = dao;
    }

    public List<Customer> getCustomers() throws SQLException {
        return dao.findAll();
    }

    public void add(String name, String phone, String email, String identity, String address) throws SQLException {
        validate(name, phone);
        dao.add(name.trim(), phone.trim(), email, identity, address);
    }

    public void update(long id, String name, String phone, String email, String identity, String address) throws SQLException {
        if (id <= 0) throw new IllegalArgumentException("Chưa chọn khách hàng.");
        validate(name, phone);
        dao.update(id, name.trim(), phone.trim(), email, identity, address);
    }

    private void validate(String name, String phone) {
        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Tên và số điện thoại là bắt buộc.");
        }
    }
}

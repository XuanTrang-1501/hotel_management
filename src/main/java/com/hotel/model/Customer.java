package com.hotel.model;

public record Customer(long id, String fullName, String phone, String email,
                       String identityNumber, String address) {}

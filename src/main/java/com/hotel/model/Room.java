package com.hotel.model;

import java.math.BigDecimal;

public record Room(long id, String roomNumber, String typeName, BigDecimal pricePerNight,
                   int floor, String status) {}

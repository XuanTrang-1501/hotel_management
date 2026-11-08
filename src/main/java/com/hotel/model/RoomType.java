package com.hotel.model;

import java.math.BigDecimal;

public record RoomType(long id, String typeName, String description, BigDecimal pricePerNight, int capacity,
                       boolean active) {}

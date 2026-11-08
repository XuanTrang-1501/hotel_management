package com.hotel.patterns.factory;
import java.math.BigDecimal;
public class CardPayment implements Payment { public String pay(BigDecimal amount) { return "Đã ghi nhận thanh toán thẻ: "+amount; } }

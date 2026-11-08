package com.hotel.patterns.factory;
import java.math.BigDecimal;
public class CashPayment implements Payment { public String pay(BigDecimal amount) { return "Đã ghi nhận thanh toán tiền mặt: "+amount; } }

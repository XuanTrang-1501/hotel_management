package com.hotel.patterns.adapter;
import java.math.BigDecimal;
public class LegacyBankGateway {
    public String transfer(BigDecimal amount) { return "Chuyển khoản ngân hàng đã được ghi nhận: "+amount; }
}

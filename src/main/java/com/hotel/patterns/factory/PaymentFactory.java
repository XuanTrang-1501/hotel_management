package com.hotel.patterns.factory;

import com.hotel.patterns.adapter.BankPaymentAdapter;
import com.hotel.patterns.adapter.LegacyBankGateway;

public class PaymentFactory {
    public static Payment create(String method) {
        if (method == null || method.isBlank()) {
            throw new IllegalArgumentException("Chưa chọn phương thức thanh toán.");
        }
        return switch (method.toUpperCase()) {
            case "CASH" -> new CashPayment();
            case "CARD" -> new CardPayment();
            case "BANK", "BANK_TRANSFER" -> new BankPaymentAdapter(new LegacyBankGateway());
            default -> throw new IllegalArgumentException("Phương thức thanh toán không hỗ trợ: " + method);
        };
    }
}

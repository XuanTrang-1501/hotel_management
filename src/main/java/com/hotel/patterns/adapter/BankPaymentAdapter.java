package com.hotel.patterns.adapter;
import com.hotel.patterns.factory.Payment;
import java.math.BigDecimal;
public class BankPaymentAdapter implements Payment {
    private final LegacyBankGateway gateway;
    public BankPaymentAdapter(LegacyBankGateway gateway) { this.gateway=gateway; }
    public String pay(BigDecimal amount) { return gateway.transfer(amount); }
}

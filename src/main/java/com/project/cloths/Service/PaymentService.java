package com.project.cloths.Service;

import com.project.cloths.Entity.Payment;

import java.io.UnsupportedEncodingException;

public interface PaymentService {
    Payment createPayment(String chatId, double amount);
    String createVnpayPaymentUrl(Payment payment, String ipAddress) throws UnsupportedEncodingException;
    Payment processVnpayReturn(String vnpayTransactionId, String status);
}
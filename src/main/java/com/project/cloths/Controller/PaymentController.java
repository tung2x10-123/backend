package com.project.cloths.Controller;

import com.project.cloths.Entity.Cart;
import com.project.cloths.Entity.Payment;
import com.project.cloths.Service.CartService;
import com.project.cloths.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CartService cartService;

    @PostMapping("/vnpay/create")
    public ResponseEntity<String> createPayment(
            @RequestParam String chatId,
            HttpServletRequest request) throws UnsupportedEncodingException {
        Cart cart = cartService.getCart(chatId);
        if (cart == null || cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        Payment payment = paymentService.createPayment(chatId, total);
        String ipAddress = request.getRemoteAddr();
        String paymentUrl = paymentService.createVnpayPaymentUrl(payment, ipAddress);
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/vnpay/return")
    public ResponseEntity<String> vnpayReturn(
            @RequestParam("vnp_TxnRef") String vnp_TxnRef,
            @RequestParam("vnp_ResponseCode") String responseCode) {
        String status = "FAILED";
        if ("00".equals(responseCode)) {
            status = "SUCCESS";
        }
        Payment payment = paymentService.processVnpayReturn(vnp_TxnRef, status);

        if ("SUCCESS".equals(status)) {
            cartService.clearCart(payment.getChatId());
            return ResponseEntity.ok("Thanh toán thành công! Cảm ơn bạn đã mua sắm. ❤️");
        } else {
            return ResponseEntity.badRequest().body("Thanh toán thất bại. Vui lòng thử lại.");
        }
    }
}
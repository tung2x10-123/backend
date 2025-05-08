package com.project.cloths.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramCallbackHandler {

    private final RestTemplate restTemplate;
    private final TelegramNotificationService telegramNotificationService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public TelegramCallbackHandler(RestTemplate restTemplate, TelegramNotificationService telegramNotificationService, OrderService orderService) {
        this.restTemplate = restTemplate;
        this.telegramNotificationService = telegramNotificationService;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
    }

    public void handleCallback(String botToken, Map<String, Object> callbackQuery, String chatId) {
        System.out.println("Starting handleCallback with callbackQuery: " + callbackQuery);
        try {
            String callbackQueryId = (String) callbackQuery.get("id");
            System.out.println("Callback query ID: " + callbackQueryId);
            Map<String, Object> message = (Map<String, Object>) callbackQuery.get("message");
            System.out.println("Message from callback: " + message);
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            String callbackChatId = chat.get("id").toString();
            String callbackData = (String) callbackQuery.get("data");

            System.out.println("Received callback: queryId=" + callbackQueryId + ", chatId=" + callbackChatId + ", data=" + callbackData);

            if (callbackData == null) {
                System.out.println("Callback data is null, skipping.");
                return;
            }

            if (callbackData.startsWith("confirm_")) {
                Long orderId = Long.parseLong(callbackData.replace("confirm_", ""));
                System.out.println("Processing confirm action for orderId: " + orderId);
                handleConfirmOrder(orderId, callbackChatId);
            } else if (callbackData.startsWith("cancel_")) {
                Long orderId = Long.parseLong(callbackData.replace("cancel_", ""));
                System.out.println("Processing cancel action for orderId: " + orderId);
                handleCancelOrder(orderId, callbackChatId);
            } else {
                System.out.println("Unknown callback data: " + callbackData);
            }

            String answerUrl = "https://api.telegram.org/bot" + botToken + "/answerCallbackQuery";
            System.out.println("Preparing to answer callback with URL: " + answerUrl);
            int retryCount = 0;
            boolean answered = false;
            while (retryCount < 3 && !answered) {
                try {
                    Map<String, Object> answerRequest = new HashMap<>();
                    answerRequest.put("callback_query_id", callbackQueryId);
                    answerRequest.put("text", "Đã xử lý yêu cầu của bạn!");
                    answerRequest.put("show_alert", false);
                    System.out.println("Sending answerCallbackQuery request: " + answerRequest);
                    restTemplate.postForObject(answerUrl, answerRequest, String.class);
                    System.out.println("Successfully answered callback queryId: " + callbackQueryId);
                    answered = true;
                } catch (Exception e) {
                    retryCount++;
                    System.out.println("Failed to answer callback queryId " + callbackQueryId + " (attempt " + retryCount + "): " + e.getMessage());
                    if (retryCount == 3) {
                        System.out.println("Max retries reached for callback queryId " + callbackQueryId);
                    }
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.out.println("Error processing callback: " + e.getMessage());
        }
    }

    private void handleConfirmOrder(Long orderId, String chatId) {
        try {
            System.out.println("Starting to confirm order: " + orderId);
            Map<String, Object> statusRequest = new HashMap<>();
            statusRequest.put("status", "SHIPPED");
            orderService.updateOrderStatus(orderId, statusRequest);
            System.out.println("Confirmed order: " + orderId + " successfully");
            telegramNotificationService.sendNotification("✅ Đã xác nhận đơn hàng " + orderId + "!");
        } catch (Exception e) {
            System.out.println("Failed to confirm order " + orderId + ": " + e.getMessage());
            telegramNotificationService.sendNotification("❌ Lỗi khi xác nhận đơn hàng " + orderId + ": " + e.getMessage());
        }
    }

    private void handleCancelOrder(Long orderId, String chatId) {
        try {
            System.out.println("Starting to cancel order: " + orderId);
            Map<String, Object> statusRequest = new HashMap<>();
            statusRequest.put("status", "CANCELLED");
            orderService.updateOrderStatus(orderId, statusRequest);
            System.out.println("Cancelled order: " + orderId + " successfully");
            telegramNotificationService.sendNotification("❌ Đã hủy đơn hàng " + orderId + "!");
        } catch (Exception e) {
            System.out.println("Failed to cancel order " + orderId + ": " + e.getMessage());
            telegramNotificationService.sendNotification("❌ Lỗi khi hủy đơn hàng " + orderId + ": " + e.getMessage());
        }
    }
}
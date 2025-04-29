package com.project.cloths.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TelegramCallbackHandler {

    private final RestTemplate restTemplate;
    private final TelegramNotificationService telegramNotificationService;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private long lastUpdateId = 0;

    @Autowired
    public TelegramCallbackHandler(RestTemplate restTemplate, TelegramNotificationService telegramNotificationService, OrderService orderService) {
        this.restTemplate = restTemplate;
        this.telegramNotificationService = telegramNotificationService;
        this.orderService = orderService;
        this.objectMapper = new ObjectMapper();
    }

    public void handleCallback(String botToken, String chatId) {
        String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/getUpdates?offset=" + (lastUpdateId + 1);

        try {
            Map<String, Object> response = restTemplate.getForObject(telegramApiUrl, Map.class);
            if (response == null || !response.containsKey("result")) {
                System.out.println("No updates found from Telegram.");
                return;
            }

            List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
            for (Map<String, Object> update : updates) {
                if (!update.containsKey("callback_query")) {
                    continue;
                }

                long updateId = Long.parseLong(update.get("update_id").toString());
                if (updateId > lastUpdateId) {
                    lastUpdateId = updateId;
                }

                Map<String, Object> callbackQuery = (Map<String, Object>) update.get("callback_query");
                Map<String, Object> message = (Map<String, Object>) callbackQuery.get("message");
                String callbackData = (String) callbackQuery.get("data");

                if (callbackData.startsWith("confirm_")) {
                    Long orderId = Long.parseLong(callbackData.replace("confirm_", ""));
                    handleConfirmOrder(orderId, chatId);
                } else if (callbackData.startsWith("cancel_")) {
                    Long orderId = Long.parseLong(callbackData.replace("cancel_", ""));
                    handleCancelOrder(orderId, chatId);
                }

                String callbackQueryId = (String) callbackQuery.get("id");
                String answerUrl = "https://api.telegram.org/bot" + botToken + "/answerCallbackQuery?callback_query_id=" + callbackQueryId;
                try {
                    restTemplate.getForObject(answerUrl, String.class);
                } catch (Exception e) {
                    System.out.println("Failed to answer callback query (possibly too old): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to handle Telegram callback: " + e.getMessage());
        }
    }

    private void handleConfirmOrder(Long orderId, String chatId) {
        try {
            Map<String, Object> statusRequest = new HashMap<>();
            statusRequest.put("status", "SHIPPED");
            orderService.updateOrderStatus(orderId, statusRequest);
            // Không gửi thông báo ở đây nữa, để OrderServiceImpl xử lý
        } catch (Exception e) {
            System.out.println("Failed to confirm order " + orderId + ": " + e.getMessage());
            telegramNotificationService.sendNotification("❌ Lỗi khi xác nhận đơn hàng " + orderId + ": " + e.getMessage());
        }
    }

    private void handleCancelOrder(Long orderId, String chatId) {
        try {
            Map<String, Object> statusRequest = new HashMap<>();
            statusRequest.put("status", "CANCELLED");
            orderService.updateOrderStatus(orderId, statusRequest);
            // Không gửi thông báo ở đây nữa, để OrderServiceImpl xử lý
        } catch (Exception e) {
            System.out.println("Failed to cancel order " + orderId + ": " + e.getMessage());
            telegramNotificationService.sendNotification("❌ Lỗi khi hủy đơn hàng " + orderId + ": " + e.getMessage());
        }
    }
}
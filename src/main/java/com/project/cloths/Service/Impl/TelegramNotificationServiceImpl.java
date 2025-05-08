package com.project.cloths.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.cloths.Entity.OrderStatus;
import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.Service.TelegramNotificationService;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    private final RestTemplate restTemplate;
    private final TelegramConfigRepository telegramConfigRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public TelegramNotificationServiceImpl(RestTemplate restTemplate, TelegramConfigRepository telegramConfigRepository) {
        this.restTemplate = restTemplate;
        this.telegramConfigRepository = telegramConfigRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendNotification(String message) {
        sendNotificationWithButtons(message, null, null);
    }

    @Override
    public void sendNotificationWithButtons(String message, Long orderId, OrderStatus orderStatus) {
        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configs found. Skipping: " + message);
            return;
        }

        for (TelegramConfig config : configs) {
            String chatId = config.getChatId();
            String botToken = config.getBotToken();
            String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            System.out.println("Preparing to send notification to chatId " + chatId + " with botToken " + botToken);
            try {
                Map<String, Object> telegramRequest = new HashMap<>();
                telegramRequest.put("chat_id", chatId);
                telegramRequest.put("text", message);

                if (orderId != null && orderStatus != null) {
                    List<Map<String, Object>> buttonRow = new ArrayList<>();

                    if (orderStatus == OrderStatus.PENDING) {
                        Map<String, Object> button1 = new HashMap<>();
                        button1.put("text", "Xác nhận đơn");
                        button1.put("callback_data", "confirm_" + orderId);
                        buttonRow.add(button1);

                        Map<String, Object> button2 = new HashMap<>();
                        button2.put("text", "Hủy đơn");
                        button2.put("callback_data", "cancel_" + orderId);
                        buttonRow.add(button2);
                    } else if (orderStatus == OrderStatus.SHIPPED) {
                        Map<String, Object> button2 = new HashMap<>();
                        button2.put("text", "Hủy đơn");
                        button2.put("callback_data", "cancel_" + orderId);
                        buttonRow.add(button2);
                    }

                    if (!buttonRow.isEmpty()) {
                        List<List<Map<String, Object>>> inlineKeyboard = new ArrayList<>();
                        inlineKeyboard.add(buttonRow);

                        Map<String, Object> replyMarkup = new HashMap<>();
                        replyMarkup.put("inline_keyboard", inlineKeyboard);

                        telegramRequest.put("reply_markup", replyMarkup);
                    }
                }

                System.out.println("Sending notification request: " + telegramRequest);
                String response = restTemplate.postForObject(telegramApiUrl, telegramRequest, String.class);
                System.out.println("Notification response: " + response);
                if (response != null && response.contains("\"ok\":true")) {
                    System.out.println("Sent notification to chatId " + chatId + " successfully: " + message);
                } else {
                    System.out.println("Failed to send notification to chatId " + chatId + ". Response: " + response);
                }
            } catch (Exception e) {
                System.out.println("Failed to send to chatId " + chatId + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void sendNotificationWithPhoto(String message, String photoUrl, Long orderId, OrderStatus orderStatus) {
        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configs found. Skipping: " + message);
            return;
        }

        if (photoUrl == null || photoUrl.isEmpty() || !photoUrl.startsWith("http")) {
            System.out.println("Invalid photoUrl " + photoUrl + ". Falling back.");
            sendNotificationWithButtons(message, orderId, orderStatus);
            return;
        }

        for (TelegramConfig config : configs) {
            String chatId = config.getChatId();
            String botToken = config.getBotToken();
            String telegramApiUrl = "https://api.telegram.org/bot" + botToken + "/sendPhoto";
            System.out.println("Preparing to send photo notification to chatId " + chatId + " with botToken " + botToken);
            try {
                Map<String, Object> telegramRequest = new HashMap<>();
                telegramRequest.put("chat_id", chatId);
                telegramRequest.put("photo", photoUrl);
                telegramRequest.put("caption", message);

                if (orderId != null && orderStatus != null) {
                    List<Map<String, Object>> buttonRow = new ArrayList<>();

                    if (orderStatus == OrderStatus.PENDING) {
                        Map<String, Object> button1 = new HashMap<>();
                        button1.put("text", "Xác nhận đơn");
                        button1.put("callback_data", "confirm_" + orderId);
                        buttonRow.add(button1);

                        Map<String, Object> button2 = new HashMap<>();
                        button2.put("text", "Hủy đơn");
                        button2.put("callback_data", "cancel_" + orderId);
                        buttonRow.add(button2);
                    } else if (orderStatus == OrderStatus.SHIPPED) {
                        Map<String, Object> button2 = new HashMap<>();
                        button2.put("text", "Hủy đơn");
                        button2.put("callback_data", "cancel_" + orderId);
                        buttonRow.add(button2);
                    }

                    if (!buttonRow.isEmpty()) {
                        List<List<Map<String, Object>>> inlineKeyboard = new ArrayList<>();
                        inlineKeyboard.add(buttonRow);

                        Map<String, Object> replyMarkup = new HashMap<>();
                        replyMarkup.put("inline_keyboard", inlineKeyboard);

                        telegramRequest.put("reply_markup", replyMarkup);
                    }
                }

                System.out.println("Sending photo notification request: " + telegramRequest);
                String response = restTemplate.postForObject(telegramApiUrl, telegramRequest, String.class);
                System.out.println("Photo notification response: " + response);
                if (response != null && response.contains("\"ok\":true")) {
                    System.out.println("Sent photo to chatId " + chatId + " successfully with " + photoUrl + ": " + message);
                } else {
                    System.out.println("Failed to send photo to chatId " + chatId + ". Response: " + response);
                }
            } catch (Exception e) {
                System.out.println("Failed to send photo to chatId " + chatId + " with " + photoUrl + ": " + e.getMessage());
                sendNotificationWithButtons(message, orderId, orderStatus);
            }
        }
    }
}
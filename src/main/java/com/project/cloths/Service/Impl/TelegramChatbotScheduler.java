package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.Service.TelegramChatbotService;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class TelegramChatbotScheduler {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    @Autowired
    private TelegramChatbotService telegramChatbotService;

    private long lastUpdateId = 0;

    @Scheduled(fixedRate = 5000)
    public void checkChatbotMessages() {
        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configurations found in database. Cannot check chatbot messages.");
            return;
        }

        TelegramConfig config = configs.get(0);
        String supportBotToken = config.getBotToken();
        if (supportBotToken == null || supportBotToken.isEmpty()) {
            System.out.println("Support bot token not found in TelegramConfig. Cannot check chatbot messages.");
            return;
        }

        String telegramApiUrl = "https://api.telegram.org/bot" + supportBotToken + "/getUpdates?offset=" + (lastUpdateId + 1);

        try {
            Map<String, Object> response = restTemplate.getForObject(telegramApiUrl, Map.class);
            if (response == null || !response.containsKey("result")) {
                System.out.println("No updates found from Telegram chatbot.");
                return;
            }

            List<Map<String, Object>> updates = (List<Map<String, Object>>) response.get("result");
            for (Map<String, Object> update : updates) {
                long updateId = Long.parseLong(update.get("update_id").toString());
                if (updateId > lastUpdateId) {
                    lastUpdateId = updateId;
                }

                if (!update.containsKey("message")) {
                    continue;
                }

                Map<String, Object> message = (Map<String, Object>) update.get("message");
                Map<String, Object> chat = (Map<String, Object>) message.get("chat");
                String chatId = chat.get("id").toString();
                String text = (String) message.get("text");

                if (text != null && !text.isEmpty()) {
                    telegramChatbotService.setCurrentBotToken(supportBotToken);
                    String reply = telegramChatbotService.handleIncomingMessage(chatId, text);
                    sendReply(supportBotToken, chatId, reply);
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to check Telegram chatbot messages: " + e.getMessage());
        }
    }

    private void sendReply(String botToken, String chatId, String reply) {
        String sendMessageUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + reply;
        try {
            restTemplate.getForObject(sendMessageUrl, String.class);
        } catch (Exception e) {
            System.out.println("Failed to send reply to chat " + chatId + ": " + e.getMessage());
        }
    }
}
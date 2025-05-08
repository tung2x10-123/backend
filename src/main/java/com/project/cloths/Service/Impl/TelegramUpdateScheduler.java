package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct; // Sửa từ javax sang jakarta
import java.util.List;
import java.util.Map;

@Component
public class TelegramUpdateScheduler {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    @Value("${server.base-url}")
    private String serverBaseUrl;

    private boolean webhookSet = false;

    @PostConstruct // Sử dụng jakarta.annotation.PostConstruct
    public void init() {
        setWebhook();
    }

    private void setWebhook() {
        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configs found in database.");
            return;
        }

        TelegramConfig config = configs.get(0);
        String botToken = config.getBotToken();
        if (botToken == null || botToken.isEmpty()) {
            System.out.println("Bot token not found in TelegramConfig.");
            return;
        }

        String webhookUrl = serverBaseUrl + "/telegram/webhook";
        String setWebhookUrl = "https://api.telegram.org/bot" + botToken + "/setWebhook?url=" + webhookUrl;
        try {
            Map<String, Object> response = restTemplate.getForObject(setWebhookUrl, Map.class);
            System.out.println("Set webhook response: " + response);
            if (response != null && (Boolean) response.get("ok")) {
                System.out.println("Webhook set successfully to: " + webhookUrl);
                webhookSet = true;
            } else {
                System.out.println("Failed to set webhook.");
            }
        } catch (Exception e) {
            System.out.println("Failed to set webhook: " + e.getMessage());
        }
    }

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        // Có thể thêm logic nếu cần
    }
}
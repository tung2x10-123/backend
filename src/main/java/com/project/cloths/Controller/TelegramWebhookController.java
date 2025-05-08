package com.project.cloths.Controller;

import com.project.cloths.Entity.TelegramConfig;
import com.project.cloths.Service.Impl.TelegramCallbackHandler;
import com.project.cloths.Service.OrderService;
import com.project.cloths.Service.TelegramChatbotService;
import com.project.cloths.repository.TelegramConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
public class TelegramWebhookController {

    @Autowired
    private TelegramConfigRepository telegramConfigRepository;

    @Autowired
    private TelegramChatbotService telegramChatbotService;

    @Autowired
    private TelegramCallbackHandler telegramCallbackHandler;

    @Autowired
    private OrderService orderService;

    @PostMapping("/telegram/webhook")
    public void handleWebhook(@RequestBody Map<String, Object> update) {
        System.out.println("Received webhook update: " + update);

        List<TelegramConfig> configs = telegramConfigRepository.findAll();
        if (configs.isEmpty()) {
            System.out.println("No Telegram configs found in database.");
            return;
        }

        TelegramConfig config = configs.get(0);
        String botToken = config.getBotToken();
        String chatId = config.getChatId();

        if (update.containsKey("message")) {
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            String messageChatId = chat.get("id").toString();
            String text = (String) message.get("text");

            if (text != null && !text.isEmpty()) {
                telegramChatbotService.setCurrentBotToken(botToken);
                String reply = telegramChatbotService.handleIncomingMessage(messageChatId, text);
                sendReply(botToken, messageChatId, reply);
            }
        }

        if (update.containsKey("callback_query")) {
            System.out.println("Found callback_query in webhook update: " + update);
            telegramCallbackHandler.handleCallback(botToken, (Map<String, Object>) update.get("callback_query"), chatId);
        }
    }

    private void sendReply(String botToken, String chatId, String reply) {
        String sendMessageUrl = "https://api.telegram.org/bot" + botToken + "/sendMessage?chat_id=" + chatId + "&text=" + reply;
        try {
            new RestTemplate().getForObject(sendMessageUrl, String.class);
            System.out.println("Sent reply to chatId " + chatId + ": " + reply);
        } catch (Exception e) {
            System.out.println("Failed to send reply to chat " + chatId + ": " + e.getMessage());
        }
    }
}
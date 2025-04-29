package com.project.cloths.Service;

public interface TelegramChatbotService {
    String handleIncomingMessage(String chatId, String message);
    String getOrderStatusMessage(Long orderId);
    void saveChatId(String chatId, String botToken); // Thêm phương thức này
    void setCurrentBotToken(String botToken); // Thêm để set botToken
}
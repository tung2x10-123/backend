package com.project.cloths.Controller;

import com.project.cloths.Service.TelegramChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final TelegramChatbotService telegramChatbotService;

    public ChatbotController(TelegramChatbotService telegramChatbotService) {
        this.telegramChatbotService = telegramChatbotService;
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> request) {
        String chatId = request.get("chatId");
        String message = request.get("message");

        if (chatId == null || message == null || chatId.isEmpty() || message.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "chatId and message are required");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String reply = telegramChatbotService.handleIncomingMessage(chatId, message);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("reply", reply);
            return ResponseEntity.ok(successResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process message");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
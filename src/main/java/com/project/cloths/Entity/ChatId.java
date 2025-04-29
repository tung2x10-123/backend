package com.project.cloths.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class ChatId {
    @Id
    private Long chatId;

    private LocalDateTime createdAt;

    public ChatId() {
        this.createdAt = LocalDateTime.now();
    }

    public ChatId(Long chatId) {
        this.chatId = chatId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
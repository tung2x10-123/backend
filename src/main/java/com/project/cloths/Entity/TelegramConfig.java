package com.project.cloths.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telegram_configs")
public class TelegramConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String botToken;
    private String chatId;

    @Override
    public String toString() {
        return "TelegramConfig{id=" + id + ", botToken='[hidden]', chatId='" + chatId + "'}";
    }
}
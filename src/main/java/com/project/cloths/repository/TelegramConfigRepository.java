package com.project.cloths.repository;

import com.project.cloths.Entity.TelegramConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramConfigRepository extends JpaRepository<TelegramConfig, Long> {
    TelegramConfig findByBotToken(String botToken);
}
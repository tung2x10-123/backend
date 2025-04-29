package com.project.cloths.repository;

import com.project.cloths.Entity.ChatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatIdRepository extends JpaRepository<ChatId, Long> {
}
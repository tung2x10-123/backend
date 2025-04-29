package com.project.cloths.repository;

import com.project.cloths.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByChatId(String chatId);
}
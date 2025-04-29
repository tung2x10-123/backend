package com.project.cloths.Service;

import com.project.cloths.Entity.Cart;

public interface CartService {
    Cart addToCart(String chatId, Long productId, int quantity);
    Cart getCart(String chatId);
    Cart clearCart(String chatId);
}
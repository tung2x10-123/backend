package com.project.cloths.Service.Impl;

import com.project.cloths.Entity.Cart;
import com.project.cloths.Entity.CartItem;
import com.project.cloths.Entity.Product;
import com.project.cloths.Service.CartService;
import com.project.cloths.repository.CartRepository;
import com.project.cloths.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    @Transactional
    public Cart addToCart(String chatId, Long productId, int quantity) {
        Cart cart = cartRepository.findByChatId(chatId);
        if (cart == null) {
            cart = new Cart();
            cart.setChatId(chatId);
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) { // Thay isEmpty() bằng !isPresent()
            throw new RuntimeException("Product not found with id: " + productId);
        }
        Product product = productOpt.get();

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) { // Thay !isEmpty() bằng isPresent()
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setProduct(product);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    @Override
    @Transactional
    public Cart getCart(String chatId) {
        Cart cart = cartRepository.findByChatId(chatId);
        if (cart == null) {
            cart = new Cart();
            cart.setChatId(chatId);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @Override
    @Transactional
    public Cart clearCart(String chatId) {
        Cart cart = cartRepository.findByChatId(chatId);
        if (cart != null) {
            cart.getItems().clear();
            cart = cartRepository.save(cart);
        }
        return cart;
    }
}
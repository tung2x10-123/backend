package com.project.cloths.Controller;

import com.project.cloths.Entity.Cart;
import com.project.cloths.Service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(
            @RequestParam String chatId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        try {
            Cart cart = cartService.addToCart(chatId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam String chatId) {
        Cart cart = cartService.getCart(chatId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/clear")
    public ResponseEntity<Cart> clearCart(@RequestParam String chatId) {
        Cart cart = cartService.clearCart(chatId);
        return ResponseEntity.ok(cart);
    }
}
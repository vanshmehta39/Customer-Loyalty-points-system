package com.loyalty.system.controller;

import com.loyalty.system.dto.ApiResponse;
import com.loyalty.system.dto.CartItemRequest;
import com.loyalty.system.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCart(
            @RequestParam Long userId,
            @RequestParam(required = false) String couponCode) {
        try {
            Map<String, Object> summary = cartService.getCartSummary(userId, couponCode);
            return ResponseEntity.ok(ApiResponse.ok("Cart loaded.", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<String>> addItem(
            @RequestParam Long userId,
            @Valid @RequestBody CartItemRequest req) {
        try {
            cartService.addToCart(userId, req.getProductId(), req.getQuantity());
            return ResponseEntity.ok(ApiResponse.ok("Product added to cart!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<String>> updateItem(
            @RequestParam Long userId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        try {
            cartService.updateQuantity(userId, itemId, quantity);
            return ResponseEntity.ok(ApiResponse.ok("Cart updated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<String>> removeItem(
            @RequestParam Long userId,
            @PathVariable Long itemId) {
        try {
            cartService.removeItem(userId, itemId);
            return ResponseEntity.ok(ApiResponse.ok("Item removed from cart."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<String>> clearCart(@RequestParam Long userId) {
        try {
            cartService.clearCart(userId);
            return ResponseEntity.ok(ApiResponse.ok("Cart cleared."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

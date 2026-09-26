package com.loyalty.system.controller;

import com.loyalty.system.dto.ApiResponse;
import com.loyalty.system.dto.CheckoutRequest;
import com.loyalty.system.dto.OrderResponse;
import com.loyalty.system.model.Order;
import com.loyalty.system.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestParam Long userId,
            @Valid @RequestBody CheckoutRequest req) {
        try {
            OrderResponse resp = orderService.placeOrder(userId, req);
            return ResponseEntity.ok(ApiResponse.ok("Order placed successfully! Loyalty points credited.", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to complete checkout: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getUserOrders(@RequestParam Long userId) {
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(ApiResponse.ok("Orders loaded.", orders));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.ok("Order details loaded.", order));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

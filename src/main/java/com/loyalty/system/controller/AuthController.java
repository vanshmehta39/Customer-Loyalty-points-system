package com.loyalty.system.controller;

import com.loyalty.system.dto.*;
import com.loyalty.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        try {
            AuthResponse resp = userService.register(req);
            return ResponseEntity.ok(ApiResponse.ok("Registration successful! Welcome to LoyaltyHub.", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to register. Please check your details."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest req) {
        try {
            AuthResponse resp = userService.authenticate(req);
            return ResponseEntity.ok(ApiResponse.ok("Login successful.", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Authentication failed."));
        }
    }

    @PostMapping("/admin-login")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(@Valid @RequestBody AuthRequest req) {
        try {
            AuthResponse resp = userService.authenticate(req);
            if (!"ADMIN".equalsIgnoreCase(resp.getRole())) {
                return ResponseEntity.status(403).body(ApiResponse.error("Access denied. Admin privileges required."));
            }
            return ResponseEntity.ok(ApiResponse.ok("Admin authentication successful.", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Admin authentication failed."));
        }
    }
}

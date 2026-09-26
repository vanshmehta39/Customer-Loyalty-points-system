package com.loyalty.system.controller;

import com.loyalty.system.dto.*;
import com.loyalty.system.model.Product;
import com.loyalty.system.model.Reward;
import com.loyalty.system.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final RewardService rewardService;
    private final NotificationService notificationService;

    public AdminController(AdminService adminService,
                           ProductService productService,
                           RewardService rewardService,
                           NotificationService notificationService) {
        this.adminService = adminService;
        this.productService = productService;
        this.rewardService = rewardService;
        this.notificationService = notificationService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AdminSummaryDto>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok("Admin analytics loaded.", adminService.getAdminSummary()));
    }

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getCustomers(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(ApiResponse.ok("Customers list loaded.", adminService.getAllCustomers(query)));
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCustomerDetails(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Customer details loaded.", adminService.getCustomerDetails(id)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Products Management
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.ok("All products loaded.", productService.getAllProductsForAdmin()));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<Product>> createProduct(@Valid @RequestBody ProductDto dto) {
        try {
            Product p = productService.createProduct(dto);
            return ResponseEntity.ok(ApiResponse.ok("Product created successfully.", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        try {
            Product p = productService.updateProduct(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Product updated successfully.", p));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/products/{id}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleProduct(@PathVariable Long id) {
        try {
            productService.toggleProductActive(id);
            return ResponseEntity.ok(ApiResponse.ok("Product status toggled."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.ok(ApiResponse.ok("Product deactivated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Rewards Management
    @GetMapping("/rewards")
    public ResponseEntity<ApiResponse<List<Reward>>> getAllRewards() {
        return ResponseEntity.ok(ApiResponse.ok("All rewards loaded.", rewardService.getAllRewardsForAdmin()));
    }

    @PostMapping("/rewards")
    public ResponseEntity<ApiResponse<Reward>> createReward(@Valid @RequestBody RewardDto dto) {
        try {
            Reward r = rewardService.createReward(dto);
            return ResponseEntity.ok(ApiResponse.ok("Reward created successfully.", r));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/rewards/{id}")
    public ResponseEntity<ApiResponse<Reward>> updateReward(@PathVariable Long id, @Valid @RequestBody RewardDto dto) {
        try {
            Reward r = rewardService.updateReward(id, dto);
            return ResponseEntity.ok(ApiResponse.ok("Reward updated successfully.", r));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/rewards/{id}/toggle")
    public ResponseEntity<ApiResponse<String>> toggleReward(@PathVariable Long id) {
        try {
            rewardService.toggleRewardActive(id);
            return ResponseEntity.ok(ApiResponse.ok("Reward status toggled."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/rewards/{id}")
    public ResponseEntity<ApiResponse<String>> deleteReward(@PathVariable Long id) {
        try {
            rewardService.deleteReward(id);
            return ResponseEntity.ok(ApiResponse.ok("Reward deactivated."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Notification broadcast
    @PostMapping("/notifications/broadcast")
    public ResponseEntity<ApiResponse<String>> broadcastNotification(@Valid @RequestBody AdminNotificationRequest req) {
        try {
            notificationService.createNotification(req.getUserId(), req.getTitle(), req.getMessage(), "PROMO");
            String msg = req.getUserId() == null ? "Notification broadcasted to all customers!" : "Notification sent to customer ID " + req.getUserId();
            return ResponseEntity.ok(ApiResponse.ok(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

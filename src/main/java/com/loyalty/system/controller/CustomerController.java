package com.loyalty.system.controller;

import com.loyalty.system.dto.*;
import com.loyalty.system.model.MembershipTier;
import com.loyalty.system.model.PointsTransaction;
import com.loyalty.system.model.User;
import com.loyalty.system.repository.PointsTransactionRepository;
import com.loyalty.system.service.MembershipTierService;
import com.loyalty.system.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final UserService userService;
    private final MembershipTierService tierService;
    private final PointsTransactionRepository transactionRepository;

    public CustomerController(UserService userService,
                              MembershipTierService tierService,
                              PointsTransactionRepository transactionRepository) {
        this.userService = userService;
        this.tierService = tierService;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboard(@RequestParam Long userId) {
        try {
            DashboardSummaryDto summary = userService.getDashboardSummary(userId);
            return ResponseEntity.ok(ApiResponse.ok("Dashboard data loaded successfully.", summary));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<User>> getProfile(@RequestParam Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(ApiResponse.ok("Profile loaded.", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<User>> updateProfile(@RequestParam Long userId,
                                                          @RequestBody UserProfileDto dto) {
        try {
            User updated = userService.updateProfile(userId, dto.getFullName(), dto.getPhone(), dto.getCity(), dto.getBirthDate());
            return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@RequestParam Long userId,
                                                              @Valid @RequestBody ChangePasswordRequest req) {
        try {
            userService.changePassword(userId, req);
            return ResponseEntity.ok(ApiResponse.ok("Password changed successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/claim-birthday-bonus")
    public ResponseEntity<ApiResponse<String>> claimBirthdayBonus(@RequestParam Long userId) {
        try {
            userService.claimBirthdayBonus(userId);
            return ResponseEntity.ok(ApiResponse.ok("🎉 Birthday bonus credited! +100 points have been added to your balance."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/points-history")
    public ResponseEntity<ApiResponse<List<PointsTransaction>>> getPointsHistory(@RequestParam Long userId) {
        try {
            List<PointsTransaction> history = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(ApiResponse.ok("Points history loaded.", history));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tiers")
    public ResponseEntity<ApiResponse<List<MembershipTier>>> getMembershipTiers() {
        return ResponseEntity.ok(ApiResponse.ok("Tiers loaded.", tierService.getAllTiers()));
    }
}

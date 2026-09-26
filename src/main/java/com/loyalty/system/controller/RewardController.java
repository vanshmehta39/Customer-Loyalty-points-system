package com.loyalty.system.controller;

import com.loyalty.system.dto.ApiResponse;
import com.loyalty.system.dto.RedeemRewardRequest;
import com.loyalty.system.model.RedeemedReward;
import com.loyalty.system.model.Reward;
import com.loyalty.system.service.RewardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Reward>>> getAllRewards() {
        return ResponseEntity.ok(ApiResponse.ok("Rewards catalog loaded.", rewardService.getAllActiveRewards()));
    }

    @PostMapping("/redeem")
    public ResponseEntity<ApiResponse<RedeemedReward>> redeemReward(
            @RequestParam Long userId,
            @Valid @RequestBody RedeemRewardRequest req) {
        try {
            RedeemedReward redeemed = rewardService.redeemReward(userId, req.getRewardId());
            return ResponseEntity.ok(ApiResponse.ok("Reward redeemed successfully! Coupon code generated.", redeemed));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to redeem reward: " + e.getMessage()));
        }
    }

    @GetMapping("/my-rewards")
    public ResponseEntity<ApiResponse<List<RedeemedReward>>> getMyRewards(@RequestParam Long userId) {
        try {
            List<RedeemedReward> list = rewardService.getUserRedeemedRewards(userId);
            return ResponseEntity.ok(ApiResponse.ok("Redeemed rewards loaded.", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-coupons")
    public ResponseEntity<ApiResponse<List<RedeemedReward>>> getActiveCoupons(@RequestParam Long userId) {
        try {
            List<RedeemedReward> list = rewardService.getUserActiveCoupons(userId);
            return ResponseEntity.ok(ApiResponse.ok("Active coupons loaded.", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.loyalty.system.service;

import com.loyalty.system.dto.RewardDto;
import com.loyalty.system.model.Notification;
import com.loyalty.system.model.RedeemedReward;
import com.loyalty.system.model.Reward;
import com.loyalty.system.model.User;
import com.loyalty.system.repository.NotificationRepository;
import com.loyalty.system.repository.RedeemedRewardRepository;
import com.loyalty.system.repository.RewardRepository;
import com.loyalty.system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final RedeemedRewardRepository redeemedRewardRepository;
    private final UserRepository userRepository;
    private final LoyaltyPointsService pointsService;
    private final NotificationRepository notificationRepository;

    public RewardService(RewardRepository rewardRepository,
                         RedeemedRewardRepository redeemedRewardRepository,
                         UserRepository userRepository,
                         LoyaltyPointsService pointsService,
                         NotificationRepository notificationRepository) {
        this.rewardRepository = rewardRepository;
        this.redeemedRewardRepository = redeemedRewardRepository;
        this.userRepository = userRepository;
        this.pointsService = pointsService;
        this.notificationRepository = notificationRepository;
    }

    public List<Reward> getAllActiveRewards() {
        return rewardRepository.findByActiveTrueOrderByPointsRequiredAsc();
    }

    public List<Reward> getAllRewardsForAdmin() {
        return rewardRepository.findAll();
    }

    public Reward getRewardById(Long id) {
        return rewardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found: " + id));
    }

    @Transactional
    public RedeemedReward redeemReward(Long userId, Long rewardId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Reward reward = getRewardById(rewardId);
        if (!reward.getActive()) {
            throw new IllegalArgumentException("This reward is currently unavailable.");
        }

        if (user.getPointsBalance() < reward.getPointsRequired()) {
            throw new IllegalArgumentException("Insufficient points balance. You need " +
                reward.getPointsRequired() + " points, but currently have " + user.getPointsBalance() + " points.");
        }

        // Deduct points from user balance and record transaction
        String desc = "Redeemed reward: " + reward.getName() + " (-" + reward.getPointsRequired() + " pts)";
        pointsService.deductPoints(user, reward.getPointsRequired(), desc);

        // Generate unique coupon code
        String prefix = "LOYAL";
        if ("FLAT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
            prefix = "DISC" + (int) reward.getRewardValue().doubleValue();
        } else if ("PERCENT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
            prefix = "OFF" + (int) reward.getRewardValue().doubleValue();
        } else if ("FREE_DELIVERY".equalsIgnoreCase(reward.getRewardType())) {
            prefix = "FREESHIP";
        }
        String couponCode = prefix + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(reward.getExpiryDays());
        RedeemedReward redeemed = new RedeemedReward(user, reward, couponCode, reward.getPointsRequired(), expiresAt);
        redeemed = redeemedRewardRepository.save(redeemed);

        // Create notification
        String notifMsg = "Your coupon code is: " + couponCode + ". Apply it during checkout before " +
                          expiresAt.toLocalDate() + "!";
        notificationRepository.save(new Notification(user, "🎟️ Reward Redeemed Successfully!", notifMsg, "REWARD_REDEEMED"));

        return redeemed;
    }

    public List<RedeemedReward> getUserRedeemedRewards(Long userId) {
        return redeemedRewardRepository.findByUserIdOrderByRedeemedAtDesc(userId);
    }

    public List<RedeemedReward> getUserActiveCoupons(Long userId) {
        return redeemedRewardRepository.findByUserIdAndStatusOrderByRedeemedAtDesc(userId, "ACTIVE");
    }

    @Transactional
    public Reward createReward(RewardDto dto) {
        Reward reward = new Reward(
            dto.getName().trim(),
            dto.getDescription(),
            dto.getPointsRequired(),
            dto.getRewardType().trim(),
            dto.getRewardValue(),
            dto.getIcon() != null && !dto.getIcon().isBlank() ? dto.getIcon() : "🎁",
            dto.getActive() != null ? dto.getActive() : true,
            dto.getExpiryDays() != null ? dto.getExpiryDays() : 30
        );
        return rewardRepository.save(reward);
    }

    @Transactional
    public Reward updateReward(Long id, RewardDto dto) {
        Reward reward = getRewardById(id);
        reward.setName(dto.getName().trim());
        reward.setDescription(dto.getDescription());
        reward.setPointsRequired(dto.getPointsRequired());
        reward.setRewardType(dto.getRewardType().trim());
        reward.setRewardValue(dto.getRewardValue());
        if (dto.getIcon() != null) reward.setIcon(dto.getIcon());
        if (dto.getActive() != null) reward.setActive(dto.getActive());
        if (dto.getExpiryDays() != null) reward.setExpiryDays(dto.getExpiryDays());
        return rewardRepository.save(reward);
    }

    @Transactional
    public void toggleRewardActive(Long id) {
        Reward reward = getRewardById(id);
        reward.setActive(!reward.getActive());
        rewardRepository.save(reward);
    }

    @Transactional
    public void deleteReward(Long id) {
        Reward reward = getRewardById(id);
        reward.setActive(false);
        rewardRepository.save(reward);
    }
}

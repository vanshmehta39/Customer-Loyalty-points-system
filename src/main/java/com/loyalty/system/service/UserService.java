package com.loyalty.system.service;

import com.loyalty.system.dto.*;
import com.loyalty.system.model.*;
import com.loyalty.system.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PointsTransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final RewardRepository rewardRepository;
    private final MembershipTierService tierService;
    private final LoyaltyPointsService pointsService;
    private final PasswordEncoder passwordEncoder;

    @Value("${loyalty.points.referral-bonus:150}")
    private int referralBonus;

    @Value("${loyalty.points.birthday-bonus:100}")
    private int birthdayBonus;

    public UserService(UserRepository userRepository,
                       CartRepository cartRepository,
                       PointsTransactionRepository transactionRepository,
                       NotificationRepository notificationRepository,
                       RewardRepository rewardRepository,
                       MembershipTierService tierService,
                       LoyaltyPointsService pointsService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.rewardRepository = rewardRepository;
        this.tierService = tierService;
        this.pointsService = pointsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        if (userRepository.existsByEmail(req.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        LocalDate bDate = null;
        if (req.getBirthDate() != null && !req.getBirthDate().isBlank()) {
            try {
                bDate = LocalDate.parse(req.getBirthDate().trim());
            } catch (Exception ignored) {}
        }

        User user = new User(
            req.getFullName().trim(),
            req.getEmail().trim().toLowerCase(),
            passwordEncoder.encode(req.getPassword()),
            req.getPhone() != null ? req.getPhone().trim() : null,
            bDate,
            req.getCity() != null ? req.getCity().trim() : null,
            "CUSTOMER"
        );

        // Generate unique referral code for this new user
        String refCode = "LOYAL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        user.setReferralCode(refCode);

        User savedUser = userRepository.save(user);

        // Create empty Cart for the user
        cartRepository.save(new Cart(savedUser));

        // Welcome notification
        notificationRepository.save(new Notification(
            savedUser,
            "🎉 Welcome to LoyaltyHub!",
            "Earn 10% points on every purchase, unlock exclusive tiers, and redeem discount rewards.",
            "PROMO"
        ));

        // Handle referral bonus if valid referral code provided
        if (req.getReferralCode() != null && !req.getReferralCode().isBlank()) {
            Optional<User> referrerOpt = userRepository.findByReferralCode(req.getReferralCode().trim());
            if (referrerOpt.isPresent() && !referrerOpt.get().getId().equals(savedUser.getId())) {
                User referrer = referrerOpt.get();
                pointsService.awardBonus(
                    referrer,
                    referralBonus,
                    "REFERRAL",
                    "Referral Bonus: " + savedUser.getFullName() + " joined using your referral code (" + req.getReferralCode() + ")!"
                );
            }
        }

        MembershipTier tier = tierService.getTierForPoints(savedUser.getPointsBalance());
        return new AuthResponse(
            savedUser.getId(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getPointsBalance(),
            tier.getName(),
            tier.getBadgeColor(),
            "TOKEN-" + savedUser.getId() + "-" + System.currentTimeMillis()
        );
    }

    public AuthResponse authenticate(AuthRequest req) {
        User user = userRepository.findByEmail(req.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        MembershipTier tier = tierService.getTierForPoints(user.getPointsBalance());
        return new AuthResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getPointsBalance(),
            tier.getName(),
            tier.getBadgeColor(),
            "TOKEN-" + user.getId() + "-" + System.currentTimeMillis()
        );
    }

    public DashboardSummaryDto getDashboardSummary(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        DashboardSummaryDto dto = new DashboardSummaryDto();
        dto.setUserId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setCity(user.getCity());
        dto.setReferralCode(user.getReferralCode());
        dto.setCurrentPoints(user.getPointsBalance());
        dto.setLifetimePoints(user.getLifetimePoints());
        dto.setPointsRedeemed(user.getPointsRedeemed());
        dto.setTotalSpent(user.getTotalSpent());
        dto.setBirthdayBonusAvailable(isBirthdayBonusAvailable(user));

        // Membership Tier Calculations
        MembershipTier currentTier = tierService.getTierForPoints(user.getPointsBalance());
        MembershipTier nextTier = tierService.getNextTier(currentTier);

        dto.setCurrentTier(currentTier.getName());
        dto.setBadgeColor(currentTier.getBadgeColor());
        dto.setTierIcon(currentTier.getIcon());
        dto.setDiscountPercentage(currentTier.getDiscountPercentage());
        dto.setFreeDelivery(currentTier.getFreeDelivery());

        if (nextTier != null) {
            dto.setNextTier(nextTier.getName());
            dto.setPointsToNextTier(tierService.getPointsToNextTier(user.getPointsBalance(), currentTier, nextTier));
            dto.setTierProgressPercent(tierService.getProgressPercentage(user.getPointsBalance(), currentTier, nextTier));
        } else {
            dto.setNextTier("Highest Level (Diamond)");
            dto.setPointsToNextTier(0);
            dto.setTierProgressPercent(100);
        }

        // Recent Points Activity (latest 5)
        List<PointsTransaction> txs = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream().limit(5).collect(Collectors.toList());
        dto.setRecentActivities(txs);

        // Recommended Rewards (up to 4 rewards customer can afford or is close to)
        List<Reward> rewards = rewardRepository.findByActiveTrueOrderByPointsRequiredAsc()
            .stream().limit(4).collect(Collectors.toList());
        dto.setRecommendedRewards(rewards);

        // Recent Notifications (latest 5)
        dto.setNotifications(notificationRepository.findTop5ByUserIdOrderByCreatedAtDesc(user.getId()));
        dto.setUnreadNotificationCount(notificationRepository.countByUserIdAndIsReadFalse(user.getId()));

        return dto;
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
    }

    @Transactional
    public User updateProfile(Long userId, String fullName, String phone, String city, String birthDate) {
        User user = getUserById(userId);
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (city != null) user.setCity(city.trim());
        if (birthDate != null && !birthDate.isBlank()) {
            try {
                user.setBirthDate(LocalDate.parse(birthDate.trim()));
            } catch (Exception ignored) {}
        }
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password does not match.");
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("New passwords do not match.");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void claimBirthdayBonus(Long userId) {
        User user = getUserById(userId);
        LocalDate today = LocalDate.now();
        if (user.getBirthDate() == null ||
            user.getBirthDate().getMonth() != today.getMonth() ||
            user.getBirthDate().getDayOfMonth() != today.getDayOfMonth()) {
            throw new IllegalArgumentException("Birthday bonus can only be claimed on your birthday.");
        }
        if (hasClaimedBirthdayBonusThisYear(userId, today)) {
            throw new IllegalArgumentException("Birthday bonus has already been claimed this year.");
        }
        pointsService.awardBonus(user, birthdayBonus, "BIRTHDAY_BONUS", "🎂 Happy Birthday Bonus: +100 loyalty points!");
    }

    private boolean isBirthdayBonusAvailable(User user) {
        LocalDate today = LocalDate.now();
        LocalDate birthDate = user.getBirthDate();
        return birthDate != null &&
            birthDate.getMonth() == today.getMonth() &&
            birthDate.getDayOfMonth() == today.getDayOfMonth() &&
            !hasClaimedBirthdayBonusThisYear(user.getId(), today);
    }

    private boolean hasClaimedBirthdayBonusThisYear(Long userId, LocalDate today) {
        LocalDateTime yearStart = today.withDayOfYear(1).atStartOfDay();
        LocalDateTime nextYearStart = today.plusYears(1).withDayOfYear(1).atStartOfDay();
        return transactionRepository.existsByUserIdAndTransactionTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            userId, "BIRTHDAY_BONUS", yearStart, nextYearStart);
    }
}

package com.loyalty.system.service;

import com.loyalty.system.model.*;
import com.loyalty.system.repository.NotificationRepository;
import com.loyalty.system.repository.OrderRepository;
import com.loyalty.system.repository.PointsTransactionRepository;
import com.loyalty.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoyaltyPointsService {

    private final UserRepository userRepository;
    private final PointsTransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final MembershipTierService tierService;
    private final OrderRepository orderRepository;

    @Value("${loyalty.points.earn-rate:0.10}")
    private double earnRate;

    @Value("${loyalty.points.first-purchase-bonus:200}")
    private int firstPurchaseBonus;

    public LoyaltyPointsService(UserRepository userRepository,
                                PointsTransactionRepository transactionRepository,
                                NotificationRepository notificationRepository,
                                MembershipTierService tierService,
                                OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.tierService = tierService;
        this.orderRepository = orderRepository;
    }

    /**
     * Calculates points strictly on the Java backend using:
     * points = round(purchaseAmount * 10%)
     */
    public int calculatePoints(double purchaseAmount) {
        if (purchaseAmount <= 0) return 0;
        return (int) Math.round(purchaseAmount * earnRate);
    }

    @Transactional
    public int awardPurchasePoints(User user, Order order, double purchaseAmount) {
        MembershipTier previousTier = tierService.getTierForPoints(user.getPointsBalance());

        int pointsEarned = calculatePoints(purchaseAmount);
        int newBalance = user.getPointsBalance() + pointsEarned;
        user.setPointsBalance(newBalance);
        user.setLifetimePoints(user.getLifetimePoints() + pointsEarned);
        user.setTotalSpent(user.getTotalSpent() + purchaseAmount);
        userRepository.save(user);

        // Record Points Transaction
        String desc = "Purchase order #" + order.getOrderNumber() + " - Earned " + pointsEarned + " loyalty points (10%)";
        PointsTransaction tx = new PointsTransaction(user, order, pointsEarned, "PURCHASE", desc, newBalance);
        transactionRepository.save(tx);

        // Send Purchase Notification
        String notifMsg = "You earned " + pointsEarned + " points from order #" + order.getOrderNumber() + ".";
        notificationRepository.save(new Notification(user, "🛍️ Loyalty Points Earned!", notifMsg, "POINTS_EARNED"));

        // Check if First Purchase Bonus applies (+200 points)
        boolean hasPreviousBonus = transactionRepository.existsByUserIdAndTransactionType(user.getId(), "FIRST_PURCHASE_BONUS");
        long completedOrders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).size();
        if (!hasPreviousBonus && completedOrders <= 1) {
            awardBonus(user, firstPurchaseBonus, "FIRST_PURCHASE_BONUS", "Welcome Bonus: +200 points on your first completed purchase! 🎉");
        }

        // Check Tier Upgrade
        checkAndNotifyTierUpgrade(user, previousTier);

        return pointsEarned;
    }

    @Transactional
    public void awardBonus(User user, int bonusPoints, String transactionType, String description) {
        MembershipTier previousTier = tierService.getTierForPoints(user.getPointsBalance());

        int newBalance = user.getPointsBalance() + bonusPoints;
        user.setPointsBalance(newBalance);
        user.setLifetimePoints(user.getLifetimePoints() + bonusPoints);
        userRepository.save(user);

        // Save transaction
        PointsTransaction tx = new PointsTransaction(user, null, bonusPoints, transactionType, description, newBalance);
        transactionRepository.save(tx);

        // Notification
        notificationRepository.save(new Notification(user, "🎁 Bonus Points Received!", description, "BONUS"));

        // Check Tier Upgrade
        checkAndNotifyTierUpgrade(user, previousTier);
    }

    @Transactional
    public void deductPoints(User user, int pointsToDeduct, String description) {
        if (user.getPointsBalance() < pointsToDeduct) {
            throw new IllegalArgumentException("Insufficient points balance. Required: " + pointsToDeduct + ", Available: " + user.getPointsBalance());
        }

        int newBalance = user.getPointsBalance() - pointsToDeduct;
        user.setPointsBalance(newBalance);
        user.setPointsRedeemed(user.getPointsRedeemed() + pointsToDeduct);
        userRepository.save(user);

        // Save transaction (negative points)
        PointsTransaction tx = new PointsTransaction(user, null, -pointsToDeduct, "REWARD_REDEMPTION", description, newBalance);
        transactionRepository.save(tx);
    }

    private void checkAndNotifyTierUpgrade(User user, MembershipTier previousTier) {
        MembershipTier currentTier = tierService.getTierForPoints(user.getPointsBalance());
        if (previousTier != null && currentTier != null && currentTier.getMinPoints() > previousTier.getMinPoints()) {
            String title = "🎉 Congratulations! You have reached " + currentTier.getName() + " Membership.";
            String msg = "You unlocked exclusive " + currentTier.getName() + " perks including " +
                         currentTier.getDiscountPercentage() + "% discount " +
                         (currentTier.getFreeDelivery() ? "and Free Delivery!" : "!");
            notificationRepository.save(new Notification(user, title, msg, "TIER_UPGRADE"));
        }
    }
}

package com.loyalty.system.dto;

import com.loyalty.system.model.Notification;
import com.loyalty.system.model.PointsTransaction;
import com.loyalty.system.model.Reward;

import java.util.List;

public class DashboardSummaryDto {
    private Long userId;
    private String fullName;
    private String email;
    private String city;
    private String referralCode;

    // Loyalty stats
    private Integer currentPoints;
    private Integer lifetimePoints;
    private Integer pointsRedeemed;
    private Double totalSpent;

    // Tier details
    private String currentTier;
    private String tierBadgeColor;
    private String tierIcon;
    private Double discountPercentage;
    private Boolean freeDelivery;
    private String nextTier;
    private Integer pointsToNextTier;
    private Integer tierProgressPercent;

    // Collections
    private List<PointsTransaction> recentActivities;
    private List<Reward> recommendedRewards;
    private List<Notification> notifications;
    private long unreadNotificationCount;

    public DashboardSummaryDto() {}

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }

    public Integer getCurrentPoints() { return currentPoints; }
    public void setCurrentPoints(Integer currentPoints) { this.currentPoints = currentPoints; }

    public Integer getLifetimePoints() { return lifetimePoints; }
    public void setLifetimePoints(Integer lifetimePoints) { this.lifetimePoints = lifetimePoints; }

    public Integer getPointsRedeemed() { return pointsRedeemed; }
    public void setPointsRedeemed(Integer pointsRedeemed) { this.pointsRedeemed = pointsRedeemed; }

    public Double getTotalSpent() { return totalSpent; }
    public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }

    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }

    public String getTierBadgeColor() { return tierBadgeColor; }
    public void setBadgeColor(String tierBadgeColor) { this.tierBadgeColor = tierBadgeColor; }

    public String getTierIcon() { return tierIcon; }
    public void setTierIcon(String tierIcon) { this.tierIcon = tierIcon; }

    public Double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Boolean getFreeDelivery() { return freeDelivery; }
    public void setFreeDelivery(Boolean freeDelivery) { this.freeDelivery = freeDelivery; }

    public String getNextTier() { return nextTier; }
    public void setNextTier(String nextTier) { this.nextTier = nextTier; }

    public Integer getPointsToNextTier() { return pointsToNextTier; }
    public void setPointsToNextTier(Integer pointsToNextTier) { this.pointsToNextTier = pointsToNextTier; }

    public Integer getTierProgressPercent() { return tierProgressPercent; }
    public void setTierProgressPercent(Integer tierProgressPercent) { this.tierProgressPercent = tierProgressPercent; }

    public List<PointsTransaction> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<PointsTransaction> recentActivities) { this.recentActivities = recentActivities; }

    public List<Reward> getRecommendedRewards() { return recommendedRewards; }
    public void setRecommendedRewards(List<Reward> recommendedRewards) { this.recommendedRewards = recommendedRewards; }

    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }

    public long getUnreadNotificationCount() { return unreadNotificationCount; }
    public void setUnreadNotificationCount(long unreadNotificationCount) { this.unreadNotificationCount = unreadNotificationCount; }
}

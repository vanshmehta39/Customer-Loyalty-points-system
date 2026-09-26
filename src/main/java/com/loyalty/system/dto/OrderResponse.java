package com.loyalty.system.dto;

public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private Double subtotal;
    private Double discountAmount;
    private Double deliveryCharge;
    private Double finalAmount;
    private Integer pointsEarned;
    private Integer newPointsBalance;
    private String currentTier;
    private boolean upgradedTier;
    private String tierUpgradeMessage;
    private String status;

    public OrderResponse() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(Double deliveryCharge) { this.deliveryCharge = deliveryCharge; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public Integer getPointsEarned() { return pointsEarned; }
    public void setPointsEarned(Integer pointsEarned) { this.pointsEarned = pointsEarned; }

    public Integer getNewPointsBalance() { return newPointsBalance; }
    public void setNewPointsBalance(Integer newPointsBalance) { this.newPointsBalance = newPointsBalance; }

    public String getCurrentTier() { return currentTier; }
    public void setCurrentTier(String currentTier) { this.currentTier = currentTier; }

    public boolean isUpgradedTier() { return upgradedTier; }
    public void setUpgradedTier(boolean upgradedTier) { this.upgradedTier = upgradedTier; }

    public String getTierUpgradeMessage() { return tierUpgradeMessage; }
    public void setTierUpgradeMessage(String tierUpgradeMessage) { this.tierUpgradeMessage = tierUpgradeMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

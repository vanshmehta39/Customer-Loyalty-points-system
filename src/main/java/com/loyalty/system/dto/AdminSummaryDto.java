package com.loyalty.system.dto;

import com.loyalty.system.model.Order;
import com.loyalty.system.model.PointsTransaction;

import java.util.List;

public class AdminSummaryDto {
    private long totalCustomers;
    private long totalOrders;
    private double totalRevenue;
    private long totalPointsIssued;
    private long totalPointsRedeemed;
    private long activeRewardsCount;
    private long totalProductsCount;
    private List<Order> recentOrders;
    private List<PointsTransaction> recentTransactions;

    public AdminSummaryDto() {}

    public long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(long totalCustomers) { this.totalCustomers = totalCustomers; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public long getTotalPointsIssued() { return totalPointsIssued; }
    public void setTotalPointsIssued(long totalPointsIssued) { this.totalPointsIssued = totalPointsIssued; }

    public long getTotalPointsRedeemed() { return totalPointsRedeemed; }
    public void setTotalPointsRedeemed(long totalPointsRedeemed) { this.totalPointsRedeemed = totalPointsRedeemed; }

    public long getActiveRewardsCount() { return activeRewardsCount; }
    public void setActiveRewardsCount(long activeRewardsCount) { this.activeRewardsCount = activeRewardsCount; }

    public long getTotalProductsCount() { return totalProductsCount; }
    public void setTotalProductsCount(long totalProductsCount) { this.totalProductsCount = totalProductsCount; }

    public List<Order> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<Order> recentOrders) { this.recentOrders = recentOrders; }

    public List<PointsTransaction> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<PointsTransaction> recentTransactions) { this.recentTransactions = recentTransactions; }
}

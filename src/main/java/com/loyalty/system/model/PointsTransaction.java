package com.loyalty.system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_transactions")
public class PointsTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @Column(nullable = false)
    private Integer points; // positive for earned, negative for redemption

    @Column(nullable = false, length = 40)
    private String transactionType; // PURCHASE, REWARD_REDEMPTION, BONUS, REVIEW_BONUS, REFERRAL, BIRTHDAY_BONUS, EXPIRATION

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer balanceAfter;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PointsTransaction() {
    }

    public PointsTransaction(User user, Order order, Integer points, String transactionType, String description, Integer balanceAfter) {
        this.user = user;
        this.order = order;
        this.points = points;
        this.transactionType = transactionType;
        this.description = description;
        this.balanceAfter = balanceAfter;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(Integer balanceAfter) { this.balanceAfter = balanceAfter; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

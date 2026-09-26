package com.loyalty.system.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer pointsRequired;

    @Column(nullable = false, length = 40)
    private String rewardType; // FLAT_DISCOUNT, PERCENT_DISCOUNT, FREE_DELIVERY, GIFT_VOUCHER

    @Column(nullable = false)
    private Double rewardValue;

    private String icon = "🎁";
    private Boolean active = true;
    private Integer expiryDays = 30;

    public Reward() {
    }

    public Reward(String name, String description, Integer pointsRequired, String rewardType, Double rewardValue, String icon, Boolean active, Integer expiryDays) {
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.rewardType = rewardType;
        this.rewardValue = rewardValue;
        this.icon = icon;
        this.active = active != null ? active : true;
        this.expiryDays = expiryDays != null ? expiryDays : 30;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPointsRequired() { return pointsRequired; }
    public void setPointsRequired(Integer pointsRequired) { this.pointsRequired = pointsRequired; }

    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }

    public Double getRewardValue() { return rewardValue; }
    public void setRewardValue(Double rewardValue) { this.rewardValue = rewardValue; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getExpiryDays() { return expiryDays; }
    public void setExpiryDays(Integer expiryDays) { this.expiryDays = expiryDays; }
}

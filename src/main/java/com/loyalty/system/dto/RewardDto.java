package com.loyalty.system.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RewardDto {
    private Long id;

    @NotBlank(message = "Reward name is required")
    private String name;

    private String description;

    @NotNull(message = "Points required is mandatory")
    @Min(value = 1, message = "Points required must be at least 1")
    private Integer pointsRequired;

    @NotBlank(message = "Reward type is required")
    private String rewardType; // FLAT_DISCOUNT, PERCENT_DISCOUNT, FREE_DELIVERY, GIFT_VOUCHER

    @NotNull(message = "Reward value is required")
    private Double rewardValue;

    private String icon = "🎁";
    private Boolean active = true;
    private Integer expiryDays = 30;

    public RewardDto() {}

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

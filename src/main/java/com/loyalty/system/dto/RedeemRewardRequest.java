package com.loyalty.system.dto;

import jakarta.validation.constraints.NotNull;

public class RedeemRewardRequest {

    @NotNull(message = "Reward ID is required")
    private Long rewardId;

    public Long getRewardId() { return rewardId; }
    public void setRewardId(Long rewardId) { this.rewardId = rewardId; }
}

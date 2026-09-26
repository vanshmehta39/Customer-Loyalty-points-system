package com.loyalty.system.repository;

import com.loyalty.system.model.RedeemedReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RedeemedRewardRepository extends JpaRepository<RedeemedReward, Long> {
    List<RedeemedReward> findByUserIdOrderByRedeemedAtDesc(Long userId);
    List<RedeemedReward> findByUserIdAndStatusOrderByRedeemedAtDesc(Long userId, String status);
    Optional<RedeemedReward> findByCouponCode(String couponCode);
    Optional<RedeemedReward> findByUserIdAndCouponCode(Long userId, String couponCode);
}

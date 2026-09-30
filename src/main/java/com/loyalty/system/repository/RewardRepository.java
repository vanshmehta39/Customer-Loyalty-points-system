package com.loyalty.system.repository;

import com.loyalty.system.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Long> {
    List<Reward> findByActiveTrueOrderByPointsRequiredAsc();
    long countByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}

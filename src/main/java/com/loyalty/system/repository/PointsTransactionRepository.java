package com.loyalty.system.repository;

import com.loyalty.system.model.PointsTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointsTransactionRepository extends JpaRepository<PointsTransaction, Long> {
    List<PointsTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<PointsTransaction> findAllByOrderByCreatedAtDesc();
    boolean existsByUserIdAndTransactionType(Long userId, String transactionType);
}

package com.loyalty.system.repository;

import com.loyalty.system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    Optional<Order> findByOrderNumber(String orderNumber);
    boolean existsByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0.0) FROM Order o")
    Double sumTotalRevenue();
}

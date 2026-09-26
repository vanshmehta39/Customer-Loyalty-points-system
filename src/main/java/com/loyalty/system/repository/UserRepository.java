package com.loyalty.system.repository;

import com.loyalty.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByReferralCode(String referralCode);
    List<User> findByRole(String role);
    List<User> findByRoleOrderByCreatedAtDesc(String role);

    @Query("SELECT u FROM User u WHERE u.role = 'CUSTOMER' AND (LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchCustomers(@Param("query") String query);

    long countByRole(String role);

    @Query("SELECT COALESCE(SUM(u.lifetimePoints), 0) FROM User u")
    Long sumTotalPointsIssued();

    @Query("SELECT COALESCE(SUM(u.pointsRedeemed), 0) FROM User u")
    Long sumTotalPointsRedeemed();

    @Query("SELECT COALESCE(SUM(u.totalSpent), 0.0) FROM User u")
    Double sumTotalRevenue();
}

package com.loyalty.system.repository;

import com.loyalty.system.model.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipTierRepository extends JpaRepository<MembershipTier, Long> {
    Optional<MembershipTier> findByName(String name);
    List<MembershipTier> findAllByOrderByMinPointsAsc();

    @Query("SELECT m FROM MembershipTier m WHERE :points >= m.minPoints AND (:points <= m.maxPoints OR m.maxPoints IS NULL) ORDER BY m.minPoints DESC")
    List<MembershipTier> findTierForPoints(@Param("points") Integer points);
}

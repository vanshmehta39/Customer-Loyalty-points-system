package com.loyalty.system.service;

import com.loyalty.system.model.MembershipTier;
import com.loyalty.system.repository.MembershipTierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MembershipTierService {

    private final MembershipTierRepository tierRepository;

    public MembershipTierService(MembershipTierRepository tierRepository) {
        this.tierRepository = tierRepository;
    }

    public List<MembershipTier> getAllTiers() {
        return tierRepository.findAllByOrderByMinPointsAsc();
    }

    public MembershipTier getTierForPoints(Integer points) {
        if (points == null) points = 0;
        List<MembershipTier> tiers = tierRepository.findTierForPoints(points);
        if (!tiers.isEmpty()) {
            return tiers.get(0);
        }
        // Fallback to first tier (Bronze)
        return tierRepository.findAllByOrderByMinPointsAsc().stream().findFirst().orElseGet(() ->
            new MembershipTier("Bronze", 0, 199, 0.0, false, "Basic loyalty benefits", "#cd7f32", "🥉")
        );
    }

    public MembershipTier getNextTier(MembershipTier currentTier) {
        if (currentTier == null) return null;
        List<MembershipTier> allTiers = tierRepository.findAllByOrderByMinPointsAsc();
        for (int i = 0; i < allTiers.size() - 1; i++) {
            if (allTiers.get(i).getName().equalsIgnoreCase(currentTier.getName())) {
                return allTiers.get(i + 1);
            }
        }
        return null; // At highest tier (Diamond)
    }

    public Integer getPointsToNextTier(Integer points, MembershipTier currentTier, MembershipTier nextTier) {
        if (points == null) points = 0;
        if (nextTier == null) return 0;
        int diff = nextTier.getMinPoints() - points;
        return Math.max(0, diff);
    }

    public Integer getProgressPercentage(Integer points, MembershipTier currentTier, MembershipTier nextTier) {
        if (points == null) points = 0;
        if (nextTier == null) return 100;
        int tierMin = currentTier != null ? currentTier.getMinPoints() : 0;
        int tierMax = nextTier.getMinPoints();
        int range = tierMax - tierMin;
        if (range <= 0) return 100;
        int progress = (int) Math.round(((double) (points - tierMin) / range) * 100.0);
        return Math.max(0, Math.min(100, progress));
    }
}

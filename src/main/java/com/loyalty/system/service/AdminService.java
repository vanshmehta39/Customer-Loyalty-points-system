package com.loyalty.system.service;

import com.loyalty.system.dto.AdminSummaryDto;
import com.loyalty.system.model.Order;
import com.loyalty.system.model.PointsTransaction;
import com.loyalty.system.model.User;
import com.loyalty.system.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PointsTransactionRepository transactionRepository;
    private final RewardRepository rewardRepository;
    private final ProductRepository productRepository;
    private final RedeemedRewardRepository redeemedRewardRepository;
    private final MembershipTierService tierService;

    public AdminService(UserRepository userRepository,
                        OrderRepository orderRepository,
                        PointsTransactionRepository transactionRepository,
                        RewardRepository rewardRepository,
                        ProductRepository productRepository,
                        RedeemedRewardRepository redeemedRewardRepository,
                        MembershipTierService tierService) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.transactionRepository = transactionRepository;
        this.rewardRepository = rewardRepository;
        this.productRepository = productRepository;
        this.redeemedRewardRepository = redeemedRewardRepository;
        this.tierService = tierService;
    }

    public AdminSummaryDto getAdminSummary() {
        AdminSummaryDto dto = new AdminSummaryDto();
        dto.setTotalCustomers(userRepository.countByRole("CUSTOMER"));
        dto.setTotalOrders(orderRepository.count());

        Double rev = orderRepository.sumTotalRevenue();
        dto.setTotalRevenue(rev != null ? Math.round(rev * 100.0) / 100.0 : 0.0);

        Long ptsIssued = userRepository.sumTotalPointsIssued();
        dto.setTotalPointsIssued(ptsIssued != null ? ptsIssued : 0L);

        Long ptsRedeemed = userRepository.sumTotalPointsRedeemed();
        dto.setTotalPointsRedeemed(ptsRedeemed != null ? ptsRedeemed : 0L);

        dto.setActiveRewardsCount(rewardRepository.countByActiveTrue());
        dto.setTotalProductsCount(productRepository.count());

        dto.setRecentOrders(orderRepository.findAllByOrderByCreatedAtDesc().stream().limit(8).collect(Collectors.toList()));
        dto.setRecentTransactions(transactionRepository.findAllByOrderByCreatedAtDesc().stream().limit(10).collect(Collectors.toList()));

        return dto;
    }

    public List<Map<String, Object>> getAllCustomers(String query) {
        List<User> list;
        if (query != null && !query.isBlank()) {
            list = userRepository.searchCustomers(query.trim());
        } else {
            list = userRepository.findByRoleOrderByCreatedAtDesc("CUSTOMER");
        }

        return list.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("phone", u.getPhone());
            map.put("city", u.getCity());
            map.put("pointsBalance", u.getPointsBalance());
            map.put("lifetimePoints", u.getLifetimePoints());
            map.put("pointsRedeemed", u.getPointsRedeemed());
            map.put("totalSpent", u.getTotalSpent());
            map.put("referralCode", u.getReferralCode());
            map.put("createdAt", u.getCreatedAt());
            map.put("tier", tierService.getTierForPoints(u.getPointsBalance()).getName());
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getCustomerDetails(Long customerId) {
        User user = userRepository.findById(customerId)
            .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        Map<String, Object> details = new HashMap<>();
        details.put("user", user);
        details.put("tier", tierService.getTierForPoints(user.getPointsBalance()));
        details.put("orders", orderRepository.findByUserIdOrderByCreatedAtDesc(customerId));
        details.put("transactions", transactionRepository.findByUserIdOrderByCreatedAtDesc(customerId));
        details.put("redeemedRewards", redeemedRewardRepository.findByUserIdOrderByRedeemedAtDesc(customerId));
        return details;
    }
}

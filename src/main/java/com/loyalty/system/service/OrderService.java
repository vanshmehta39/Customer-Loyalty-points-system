package com.loyalty.system.service;

import com.loyalty.system.dto.CheckoutRequest;
import com.loyalty.system.dto.OrderResponse;
import com.loyalty.system.model.*;
import com.loyalty.system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final UserRepository userRepository;
    private final MembershipTierService tierService;
    private final RedeemedRewardRepository redeemedRewardRepository;
    private final LoyaltyPointsService pointsService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        CartService cartService,
                        UserRepository userRepository,
                        MembershipTierService tierService,
                        RedeemedRewardRepository redeemedRewardRepository,
                        LoyaltyPointsService pointsService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartService = cartService;
        this.userRepository = userRepository;
        this.tierService = tierService;
        this.redeemedRewardRepository = redeemedRewardRepository;
        this.pointsService = pointsService;
    }

    @Transactional
    public OrderResponse placeOrder(Long userId, CheckoutRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Cart cart = cartService.getOrCreateCart(user);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your shopping cart is empty. Add products before placing an order.");
        }

        // Subtotal calculation
        double subtotal = 0.0;
        for (CartItem item : cartItems) {
            subtotal += (item.getProduct().getPrice() * item.getQuantity());
        }

        // Tier discount
        MembershipTier initialTier = tierService.getTierForPoints(user.getPointsBalance());
        double tierDiscount = (subtotal * initialTier.getDiscountPercentage()) / 100.0;

        // Delivery calculation
        double deliveryCharge = (initialTier.getFreeDelivery() || subtotal >= 1000.0) ? 0.0 : 100.0;

        // Coupon discount
        double couponDiscount = 0.0;
        RedeemedReward appliedCoupon = null;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            Optional<RedeemedReward> redOpt = redeemedRewardRepository.findByUserIdAndCouponCode(userId, req.getCouponCode().trim());
            if (redOpt.isPresent()) {
                RedeemedReward red = redOpt.get();
                if ("ACTIVE".equalsIgnoreCase(red.getStatus()) && red.getExpiresAt().isAfter(LocalDateTime.now())) {
                    appliedCoupon = red;
                    Reward reward = red.getReward();
                    if ("FLAT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = reward.getRewardValue();
                    } else if ("PERCENT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = (subtotal * reward.getRewardValue()) / 100.0;
                    } else if ("FREE_DELIVERY".equalsIgnoreCase(reward.getRewardType())) {
                        deliveryCharge = 0.0;
                    } else if ("GIFT_VOUCHER".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = reward.getRewardValue();
                    }
                }
            }
        }

        double totalDiscount = tierDiscount + couponDiscount;
        double finalAmount = Math.max(0.0, (subtotal - totalDiscount) + deliveryCharge);
        finalAmount = Math.round(finalAmount * 100.0) / 100.0;

        // Generate Order Number
        String orderNumber = "ORD-" + System.currentTimeMillis() % 1000000 + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        // Create Order Entity
        Order order = new Order(
            orderNumber,
            user,
            Math.round(subtotal * 100.0) / 100.0,
            Math.round(totalDiscount * 100.0) / 100.0,
            Math.round(deliveryCharge * 100.0) / 100.0,
            finalAmount,
            0, // will be updated when points awarded
            appliedCoupon != null ? appliedCoupon.getCouponCode() : null,
            req.getPaymentMethod() != null ? req.getPaymentMethod() : "UPI Simulation",
            req.getShippingAddress()
        );
        order = orderRepository.save(order);

        // Save Order Items
        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem(order, ci.getProduct(), ci.getQuantity(), ci.getProduct().getPrice());
            orderItemRepository.save(oi);
            order.getItems().add(oi);
        }

        // Award 10% Loyalty Points calculated by backend
        int pointsEarned = pointsService.awardPurchasePoints(user, order, finalAmount);
        order.setPointsEarned(pointsEarned);
        orderRepository.save(order);

        // Mark coupon as USED if one was applied
        if (appliedCoupon != null) {
            appliedCoupon.setStatus("USED");
            appliedCoupon.setUsedAt(LocalDateTime.now());
            redeemedRewardRepository.save(appliedCoupon);
        }

        // Clear Cart
        cartService.clearCart(userId);

        // Check if tier was upgraded
        MembershipTier finalTier = tierService.getTierForPoints(user.getPointsBalance());
        boolean upgraded = !finalTier.getName().equalsIgnoreCase(initialTier.getName());

        OrderResponse resp = new OrderResponse();
        resp.setOrderId(order.getId());
        resp.setOrderNumber(order.getOrderNumber());
        resp.setSubtotal(order.getSubtotal());
        resp.setDiscountAmount(order.getDiscountAmount());
        resp.setDeliveryCharge(order.getDeliveryCharge());
        resp.setFinalAmount(order.getFinalAmount());
        resp.setPointsEarned(pointsEarned);
        resp.setNewPointsBalance(user.getPointsBalance());
        resp.setCurrentTier(finalTier.getName());
        resp.setUpgradedTier(upgraded);
        if (upgraded) {
            resp.setTierUpgradeMessage("🎉 Congratulations! You have unlocked " + finalTier.getName() + " Membership!");
        }
        resp.setStatus(order.getStatus());

        return resp;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
    }
}

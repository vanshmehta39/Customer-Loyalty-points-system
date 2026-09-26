package com.loyalty.system.service;

import com.loyalty.system.model.*;
import com.loyalty.system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final MembershipTierService tierService;
    private final RedeemedRewardRepository redeemedRewardRepository;
    private final LoyaltyPointsService pointsService;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository,
                       MembershipTierService tierService,
                       RedeemedRewardRepository redeemedRewardRepository,
                       LoyaltyPointsService pointsService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.tierService = tierService;
        this.redeemedRewardRepository = redeemedRewardRepository;
        this.pointsService = pointsService;
    }

    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
            .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity) {
        if (quantity <= 0) quantity = 1;
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        if (!product.getActive()) {
            throw new IllegalArgumentException("Product is currently unavailable.");
        }

        Cart cart = getOrCreateCart(user);
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public void updateQuantity(Long userId, Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this cart item.");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
            .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

        if (!item.getCart().getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this cart item.");
        }

        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        Cart cart = getOrCreateCart(user);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public Map<String, Object> getCartSummary(Long userId, String couponCode) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Cart cart = getOrCreateCart(user);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        double subtotal = 0.0;
        for (CartItem item : items) {
            subtotal += (item.getProduct().getPrice() * item.getQuantity());
        }

        MembershipTier tier = tierService.getTierForPoints(user.getPointsBalance());
        double tierDiscountAmount = (subtotal * tier.getDiscountPercentage()) / 100.0;

        // Delivery calculation (Standard: ₹100, Free if tier has free delivery or subtotal >= ₹1000)
        double deliveryCharge = (tier.getFreeDelivery() || subtotal >= 1000.0 || subtotal == 0.0) ? 0.0 : 100.0;

        // Check redeemed coupon if provided
        double couponDiscount = 0.0;
        String couponStatusMsg = null;
        boolean couponValid = false;

        if (couponCode != null && !couponCode.isBlank()) {
            Optional<RedeemedReward> redOpt = redeemedRewardRepository.findByUserIdAndCouponCode(userId, couponCode.trim());
            if (redOpt.isPresent()) {
                RedeemedReward red = redOpt.get();
                if ("ACTIVE".equalsIgnoreCase(red.getStatus()) && red.getExpiresAt().isAfter(LocalDateTime.now())) {
                    couponValid = true;
                    Reward reward = red.getReward();
                    if ("FLAT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = reward.getRewardValue();
                        couponStatusMsg = "Applied: Flat ₹" + (int) reward.getRewardValue().doubleValue() + " Discount";
                    } else if ("PERCENT_DISCOUNT".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = (subtotal * reward.getRewardValue()) / 100.0;
                        couponStatusMsg = "Applied: " + (int) reward.getRewardValue().doubleValue() + "% Discount Coupon";
                    } else if ("FREE_DELIVERY".equalsIgnoreCase(reward.getRewardType())) {
                        deliveryCharge = 0.0;
                        couponStatusMsg = "Applied: Free Delivery Coupon";
                    } else if ("GIFT_VOUCHER".equalsIgnoreCase(reward.getRewardType())) {
                        couponDiscount = reward.getRewardValue();
                        couponStatusMsg = "Applied: Gift Voucher ₹" + (int) reward.getRewardValue().doubleValue();
                    }
                } else {
                    couponStatusMsg = "Coupon is already used or expired.";
                }
            } else {
                couponStatusMsg = "Invalid coupon code or does not belong to your account.";
            }
        }

        double totalDiscount = tierDiscountAmount + couponDiscount;
        double finalTotal = Math.max(0.0, (subtotal - totalDiscount) + deliveryCharge);

        // Calculate 10% loyalty points in backend
        int estimatedPoints = items.isEmpty() ? 0 : pointsService.calculatePoints(finalTotal);

        Map<String, Object> summary = new HashMap<>();
        summary.put("cartId", cart.getId());
        summary.put("items", items);
        summary.put("itemCount", items.stream().mapToInt(CartItem::getQuantity).sum());
        summary.put("subtotal", Math.round(subtotal * 100.0) / 100.0);
        summary.put("tierName", tier.getName());
        summary.put("tierDiscountPercent", tier.getDiscountPercentage());
        summary.put("tierDiscountAmount", Math.round(tierDiscountAmount * 100.0) / 100.0);
        summary.put("couponValid", couponValid);
        summary.put("couponDiscount", Math.round(couponDiscount * 100.0) / 100.0);
        summary.put("couponMessage", couponStatusMsg);
        summary.put("totalDiscount", Math.round(totalDiscount * 100.0) / 100.0);
        summary.put("deliveryCharge", Math.round(deliveryCharge * 100.0) / 100.0);
        summary.put("finalTotal", Math.round(finalTotal * 100.0) / 100.0);
        summary.put("estimatedPoints", estimatedPoints);

        return summary;
    }
}

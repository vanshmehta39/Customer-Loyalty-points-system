package com.loyalty.system.config;

import com.loyalty.system.model.*;
import com.loyalty.system.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MembershipTierRepository tierRepository;
    private final ProductRepository productRepository;
    private final RewardRepository rewardRepository;
    private final CartRepository cartRepository;
    private final PointsTransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           MembershipTierRepository tierRepository,
                           ProductRepository productRepository,
                           RewardRepository rewardRepository,
                           CartRepository cartRepository,
                           PointsTransactionRepository transactionRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tierRepository = tierRepository;
        this.productRepository = productRepository;
        this.rewardRepository = rewardRepository;
        this.cartRepository = cartRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedMembershipTiers();
        seedRewards();
        seedProducts();
        seedUsersAndDemoData();
    }

    private void seedMembershipTiers() {
        if (tierRepository.count() == 0) {
            List<MembershipTier> tiers = Arrays.asList(
                new MembershipTier("Bronze", 0, 199, 0.0, false, "Basic loyalty member benefits, earn 10% points on purchases", "#cd7f32", "🥉"),
                new MembershipTier("Silver", 200, 499, 2.0, false, "2% member discount, earn 10% points on purchases, exclusive seasonal sales", "#9aa0a6", "🥈"),
                new MembershipTier("Gold", 500, 999, 5.0, true, "5% member discount, Free Delivery on all orders, birthday bonus points", "#f59e0b", "🥇"),
                new MembershipTier("Platinum", 1000, 1999, 8.0, true, "8% member discount, Free Delivery, priority reward redemption, double point events", "#06b6d4", "💎"),
                new MembershipTier("Diamond", 2000, 999999, 10.0, true, "10% member discount, Free Delivery, dedicated VIP concierge, 24/7 priority support", "#8b5cf6", "👑")
            );
            tierRepository.saveAll(tiers);
            System.out.println("✅ Membership tiers seeded successfully.");
        }
    }

    private void seedRewards() {
        List<Reward> rewards = Arrays.asList(
            new Reward("₹50 Discount", "Get flat ₹50 OFF on your next order total", 100, "FLAT_DISCOUNT", 50.0, "🏷️", true, 30),
            new Reward("₹100 Discount", "Get flat ₹100 OFF on your cart value", 200, "FLAT_DISCOUNT", 100.0, "💵", true, 30),
            new Reward("Free Delivery", "Enjoy free shipping with no minimum order threshold", 250, "FREE_DELIVERY", 0.0, "🚚", true, 30),
            new Reward("5% Discount Coupon", "Save an extra 5% across your entire shopping cart", 300, "PERCENT_DISCOUNT", 5.0, "🎟️", true, 30),
            new Reward("₹250 Discount", "Get flat ₹250 instant reduction on checkout", 500, "FLAT_DISCOUNT", 250.0, "🎁", true, 45),
            new Reward("10% Discount Coupon", "Unlock a special 10% storewide checkout coupon", 600, "PERCENT_DISCOUNT", 10.0, "🌟", true, 45),
            new Reward("Premium Gift Voucher", "Redeem for an exclusive gift hamper voucher worth ₹500", 1000, "GIFT_VOUCHER", 500.0, "🏆", true, 60),
            new Reward("₹500 Shopping Voucher", "Huge ₹500 store voucher credited towards checkout", 1200, "FLAT_DISCOUNT", 500.0, "💳", true, 60),
            new Reward("₹75 Cart Discount", "Take ₹75 off your next order at checkout", 150, "FLAT_DISCOUNT", 75.0, "🏷️", true, 30),
            new Reward("₹150 Cart Discount", "Save ₹150 on your next purchase", 350, "FLAT_DISCOUNT", 150.0, "🛍️", true, 30),
            new Reward("15% Discount Coupon", "Save 15% on your next order", 850, "PERCENT_DISCOUNT", 15.0, "%", true, 45),
            new Reward("₹750 Shopping Voucher", "Apply ₹750 off a future purchase", 1600, "FLAT_DISCOUNT", 750.0, "🎫", true, 60),
            new Reward("20% Discount Coupon", "Get 20% off your next order", 2000, "PERCENT_DISCOUNT", 20.0, "%", true, 60)
        );
        List<Reward> missingRewards = rewards.stream()
            .filter(reward -> !rewardRepository.existsByNameIgnoreCase(reward.getName()))
            .toList();
        if (!missingRewards.isEmpty()) {
            rewardRepository.saveAll(missingRewards);
            System.out.println("✅ Added " + missingRewards.size() + " missing rewards.");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<Product> products = Arrays.asList(
                new Product("Sony WH-CH520 Wireless Headphones", "Electronics", "Lightweight on-ear wireless headphones with up to 50 hours battery life, multipoint connection, and customizable EQ via Headphones Connect app.", 4490.0, "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80", 4.8, 142, 45),
                new Product("Wildcraft Multi-Pocket Backpack", "Fashion", "Ergonomic 32-liter water-resistant backpack engineered with airflow back padding, durable polyester, and laptop sleeve.", 1799.0, "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&auto=format&fit=crop&q=80", 4.6, 88, 60),
                new Product("boAt Wave Call Smartwatch", "Electronics", "1.69 inch HD curved display smartwatch with Bluetooth calling, 150+ watch faces, heart rate and SpO2 monitoring.", 1699.0, "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80", 4.5, 215, 80),
                new Product("Logitech K480 Multi-Device Keyboard", "Electronics", "Versatile desk keyboard with integrated cradle stand for tablet/smartphone and easy-switch dial between 3 devices.", 2695.0, "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80", 4.7, 96, 35),
                new Product("Decorative Ceramic Planter Set", "Home", "Handcrafted premium ceramic indoor planters with drainage hole and matching bamboo trays. Ideal for succulents and desk plants.", 899.0, "https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=600&auto=format&fit=crop&q=80", 4.4, 46, 50),
                new Product("Urban Classic Denim Trucker Jacket", "Fashion", "Timeless washed blue denim jacket with brass buttons, durable dual chest pockets, and relaxed regular fit.", 2499.0, "https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=600&auto=format&fit=crop&q=80", 4.5, 64, 40),
                new Product("Stainless Steel Smart Thermos Bottle", "Lifestyle", "Double-wall vacuum insulated 500ml water flask with LED touch temperature display on cap. Keeps beverages hot or cold for 24 hours.", 999.0, "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=600&auto=format&fit=crop&q=80", 4.6, 73, 90),
                new Product("Premium Genuine Leather Bifold Wallet", "Accessories", "Handstitched full-grain leather wallet with RFID blocking layer, 8 card slots, and currency partition.", 1299.0, "https://images.unsplash.com/photo-1627123424574-724758594e93?w=600&auto=format&fit=crop&q=80", 4.7, 120, 70),
                new Product("Minimalist Nordic LED Desk Lamp", "Home", "Modern aluminum desk lamp with dimmable touch control, 3 color temperatures, and flexible gooseneck arm.", 1499.0, "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600&auto=format&fit=crop&q=80", 4.5, 39, 45),
                new Product("Ergonomic Memory Foam Back Cushion", "Lifestyle", "Therapeutic lumbar support pillow with breathable mesh cover for office chair and driving comfort.", 1199.0, "https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=600&auto=format&fit=crop&q=80", 4.6, 81, 55)
            );
            productRepository.saveAll(products);
            System.out.println("✅ Sample products seeded successfully.");
        }
    }

    private void seedUsersAndDemoData() {
        // 1. Seed Admin
        if (userRepository.findByEmail("admin@loyalty.com").isEmpty()) {
            User admin = new User(
                "System Administrator",
                "admin@loyalty.com",
                passwordEncoder.encode("admin123"),
                "+91 9876543210",
                LocalDate.of(1995, 5, 15),
                "Mumbai",
                "ADMIN"
            );
            admin.setReferralCode("ADMIN-HQ");
            userRepository.save(admin);
            System.out.println("✅ Admin user seeded: admin@loyalty.com / admin123");
        }

        // 2. Seed Demo Customer (Configured with exactly 750 points, Gold tier, as requested in prompt)
        if (userRepository.findByEmail("customer@loyalty.com").isEmpty()) {
            User customer = new User(
                "Vansh Customer",
                "customer@loyalty.com",
                passwordEncoder.encode("customer123"),
                "+91 9820011223",
                LocalDate.of(2002, 9, 26),
                "Mumbai",
                "CUSTOMER"
            );
            customer.setPointsBalance(750);
            customer.setLifetimePoints(1000);
            customer.setPointsRedeemed(250);
            customer.setTotalSpent(10000.0);
            customer.setReferralCode("LOYAL-VANSH75");
            customer = userRepository.save(customer);

            // Create Cart for customer
            cartRepository.save(new Cart(customer));

            // Seed Points History for demo customer matching prompt example:
            // Purchase +200, Reward Redeemed -250, Purchase +150
            PointsTransaction t1 = new PointsTransaction(customer, null, 450, "PURCHASE", "Order ORD-2026-1001 purchase (₹4,490) - 10% loyalty points", 450);
            PointsTransaction t2 = new PointsTransaction(customer, null, 200, "BONUS", "Welcome bonus points for first purchase", 650);
            PointsTransaction t3 = new PointsTransaction(customer, null, 200, "PURCHASE", "Order ORD-2026-1045 purchase (₹2,000) - 10% loyalty points", 850);
            PointsTransaction t4 = new PointsTransaction(customer, null, -250, "REWARD_REDEMPTION", "Free Delivery Reward redemption", 600);
            PointsTransaction t5 = new PointsTransaction(customer, null, 150, "PURCHASE", "Order ORD-2026-1090 purchase (₹1,500) - 10% loyalty points", 750);
            transactionRepository.saveAll(Arrays.asList(t1, t2, t3, t4, t5));

            // Seed Notifications for demo customer
            Notification n1 = new Notification(customer, "🎉 Congratulations! You have reached Gold Membership.", "You now enjoy 5% off on all purchases and Free Delivery on every order!", "TIER_UPGRADE");
            Notification n2 = new Notification(customer, "🛍️ You earned 150 points from your recent purchase.", "Points have been credited directly to your loyalty balance.", "POINTS_EARNED");
            Notification n3 = new Notification(customer, "🎁 You can now redeem a ₹250 discount.", "Visit the Rewards Store to exchange your points for discount coupons.", "PROMO");
            notificationRepository.saveAll(Arrays.asList(n1, n2, n3));

            System.out.println("✅ Demo customer seeded: customer@loyalty.com / customer123 (750 pts, Gold)");
        }
    }
}

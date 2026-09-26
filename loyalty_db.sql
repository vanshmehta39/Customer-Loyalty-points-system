-- ====================================================================
-- Customer Loyalty Points System Database Schema
-- Compatible with MySQL 8.0+
-- ====================================================================

CREATE DATABASE IF NOT EXISTS loyalty_db;
USE loyalty_db;

-- Drop tables if re-executing (in order of foreign key dependencies)
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS redeemed_rewards;
DROP TABLE IF EXISTS rewards;
DROP TABLE IF EXISTS points_transactions;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_items;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS membership_tiers;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    birth_date DATE,
    city VARCHAR(80),
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER', -- 'CUSTOMER', 'ADMIN'
    points_balance INT NOT NULL DEFAULT 0,
    lifetime_points INT NOT NULL DEFAULT 0,
    points_redeemed INT NOT NULL DEFAULT 0,
    total_spent DOUBLE NOT NULL DEFAULT 0.0,
    referral_code VARCHAR(30) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Membership Tiers Table
CREATE TABLE membership_tiers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    min_points INT NOT NULL,
    max_points INT,
    discount_percentage DOUBLE NOT NULL DEFAULT 0.0,
    free_delivery BOOLEAN NOT NULL DEFAULT FALSE,
    benefits TEXT,
    badge_color VARCHAR(30),
    icon VARCHAR(30)
);

-- 3. Products Table
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(60) NOT NULL,
    description TEXT,
    price DOUBLE NOT NULL,
    image_url VARCHAR(500),
    rating DOUBLE DEFAULT 4.5,
    reviews_count INT DEFAULT 0,
    stock_quantity INT DEFAULT 100,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Cart Table
CREATE TABLE cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Cart Items Table
CREATE TABLE cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- 6. Orders Table
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    subtotal DOUBLE NOT NULL,
    discount_amount DOUBLE NOT NULL DEFAULT 0.0,
    delivery_charge DOUBLE NOT NULL DEFAULT 0.0,
    final_amount DOUBLE NOT NULL,
    points_earned INT NOT NULL DEFAULT 0,
    applied_coupon_code VARCHAR(50),
    payment_method VARCHAR(50) DEFAULT 'Online Simulation',
    payment_status VARCHAR(30) DEFAULT 'PAID',
    shipping_address TEXT,
    status VARCHAR(30) DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 7. Order Items Table
CREATE TABLE order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price DOUBLE NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
);

-- 8. Points Transactions Table
CREATE TABLE points_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    points INT NOT NULL,
    transaction_type VARCHAR(40) NOT NULL, -- PURCHASE, REWARD_REDEMPTION, BONUS, REVIEW_BONUS, REFERRAL, BIRTHDAY_BONUS, EXPIRATION
    description VARCHAR(255) NOT NULL,
    balance_after INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_points_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE SET NULL
);

-- 9. Rewards Table
CREATE TABLE rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description TEXT,
    points_required INT NOT NULL,
    reward_type VARCHAR(40) NOT NULL, -- FLAT_DISCOUNT, PERCENT_DISCOUNT, FREE_DELIVERY, GIFT_VOUCHER
    reward_value DOUBLE NOT NULL,
    icon VARCHAR(40),
    active BOOLEAN DEFAULT TRUE,
    expiry_days INT DEFAULT 30
);

-- 10. Redeemed Rewards Table
CREATE TABLE redeemed_rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    reward_id BIGINT NOT NULL,
    coupon_code VARCHAR(40) NOT NULL UNIQUE,
    points_used INT NOT NULL,
    status VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, USED, EXPIRED
    redeemed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP NULL,
    CONSTRAINT fk_redeemed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_redeemed_reward FOREIGN KEY (reward_id) REFERENCES rewards(id) ON DELETE CASCADE
);

-- 11. Notifications Table
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(40) DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 12. Reviews Table
CREATE TABLE reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL,
    review_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ====================================================================
-- SEED DATA
-- ====================================================================

-- 1. Membership Tiers
INSERT INTO membership_tiers (name, min_points, max_points, discount_percentage, free_delivery, benefits, badge_color, icon) VALUES
('Bronze', 0, 199, 0.0, FALSE, 'Basic loyalty member benefits, earn 10% points on purchases', '#cd7f32', '🥉'),
('Silver', 200, 499, 2.0, FALSE, '2% member discount, earn 10% points on purchases, exclusive seasonal sales', '#9aa0a6', '🥈'),
('Gold', 500, 999, 5.0, TRUE, '5% member discount, Free Delivery on all orders, birthday bonus points', '#f59e0b', '🥇'),
('Platinum', 1000, 1999, 8.0, TRUE, '8% member discount, Free Delivery, priority reward redemption, double point events', '#06b6d4', '💎'),
('Diamond', 2000, 999999, 10.0, TRUE, '10% member discount, Free Delivery, dedicated VIP concierge, 24/7 priority support', '#8b5cf6', '👑');

-- 2. Rewards
INSERT INTO rewards (name, description, points_required, reward_type, reward_value, icon, active, expiry_days) VALUES
('₹50 Discount', 'Get flat ₹50 OFF on your next order total', 100, 'FLAT_DISCOUNT', 50.0, '🏷️', TRUE, 30),
('₹100 Discount', 'Get flat ₹100 OFF on your cart value', 200, 'FLAT_DISCOUNT', 100.0, '💵', TRUE, 30),
('Free Delivery', 'Enjoy free shipping with no minimum order threshold', 250, 'FREE_DELIVERY', 0.0, '🚚', TRUE, 30),
('5% Discount Coupon', 'Save an extra 5% across your entire shopping cart', 300, 'PERCENT_DISCOUNT', 5.0, '🎟️', TRUE, 30),
('₹250 Discount', 'Get flat ₹250 instant reduction on checkout', 500, 'FLAT_DISCOUNT', 250.0, '🎁', TRUE, 45),
('10% Discount Coupon', 'Unlock a special 10% storewide checkout coupon', 600, 'PERCENT_DISCOUNT', 10.0, '🌟', TRUE, 45),
('Premium Gift Voucher', 'Redeem for an exclusive gift hamper voucher worth ₹500', 1000, 'GIFT_VOUCHER', 500.0, '🏆', TRUE, 60),
('₹500 Shopping Voucher', 'Huge ₹500 store voucher credited towards checkout', 1200, 'FLAT_DISCOUNT', 500.0, '💳', TRUE, 60);

-- 3. Initial Users (Passwords hashed with BCrypt for 'admin123' and 'customer123')
-- admin123 hash: $2a$10$wK1m3v/XwNqyZf2Yw6v/qOmN5wX/5r3nE6u5kP5O9Z8P6bE4C7d2. (or handled via DataInitializer)
-- We will also initialize and sync via Spring Boot DataInitializer so both SQL import and auto-generation work flawlessly!

package com.loyalty.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoyaltyApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyApplication.class, args);
        System.out.println("==========================================================");
        System.out.println("🚀 Customer Loyalty Points System successfully started!");
        System.out.println("🌐 Open in your browser: http://localhost:8080");
        System.out.println("👤 Demo Customer: customer@loyalty.com / customer123");
        System.out.println("🔑 Demo Admin:    admin@loyalty.com / admin123");
        System.out.println("==========================================================");
    }
}

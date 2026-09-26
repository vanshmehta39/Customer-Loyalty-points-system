package com.loyalty.system.service;

import com.loyalty.system.dto.ReviewRequest;
import com.loyalty.system.model.Product;
import com.loyalty.system.model.Review;
import com.loyalty.system.model.User;
import com.loyalty.system.repository.ProductRepository;
import com.loyalty.system.repository.ReviewRepository;
import com.loyalty.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LoyaltyPointsService pointsService;

    @Value("${loyalty.points.review-bonus:25}")
    private int reviewBonus;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository,
                         LoyaltyPointsService pointsService) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.pointsService = pointsService;
    }

    public List<Review> getReviewsForProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId);
    }

    @Transactional
    public Review addReview(Long userId, ReviewRequest req) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Product product = productRepository.findById(req.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + req.getProductId()));

        if (reviewRepository.existsByUserIdAndProductId(userId, req.getProductId())) {
            throw new IllegalArgumentException("You have already submitted a review for this product.");
        }

        // Save Review
        Review review = new Review(user, product, req.getRating(), req.getReviewText().trim());
        review = reviewRepository.save(review);

        // Update Product average rating and count
        List<Review> allReviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
        double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(req.getRating());
        product.setRating(Math.round(avg * 10.0) / 10.0);
        product.setReviewsCount(allReviews.size());
        productRepository.save(product);

        // Award +25 review bonus loyalty points
        pointsService.awardBonus(
            user,
            reviewBonus,
            "REVIEW_BONUS",
            "Review bonus: +" + reviewBonus + " loyalty points for reviewing " + product.getName()
        );

        return review;
    }
}

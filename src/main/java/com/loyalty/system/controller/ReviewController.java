package com.loyalty.system.controller;

import com.loyalty.system.dto.ApiResponse;
import com.loyalty.system.dto.ReviewRequest;
import com.loyalty.system.model.Review;
import com.loyalty.system.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<Review>>> getProductReviews(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsForProduct(productId);
        return ResponseEntity.ok(ApiResponse.ok("Reviews loaded.", reviews));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> addReview(
            @RequestParam Long userId,
            @Valid @RequestBody ReviewRequest req) {
        try {
            Review review = reviewService.addReview(userId, req);
            return ResponseEntity.ok(ApiResponse.ok("Review submitted! +25 bonus loyalty points awarded.", review));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Failed to post review: " + e.getMessage()));
        }
    }
}

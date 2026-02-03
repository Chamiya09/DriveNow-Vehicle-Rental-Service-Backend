package com.drivenow.controller;

import com.drivenow.entity.DriverReview;
import com.drivenow.service.DriverReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DriverReviewController {
    
    private final DriverReviewService driverReviewService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverReview>> getAllReviews() {
        List<DriverReview> reviews = driverReviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody Map<String, Object> request) {
        try {
            Long bookingId = Long.valueOf(request.get("bookingId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : "";
            
            DriverReview review = driverReviewService.createReview(bookingId, userId, rating, comment);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<DriverReview>> getReviewsForDriver(@PathVariable Long driverId) {
        List<DriverReview> reviews = driverReviewService.getReviewsForDriver(driverId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<DriverReview>> getReviewsByUser(@PathVariable Long userId) {
        List<DriverReview> reviews = driverReviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<?> getReviewForBooking(@PathVariable Long bookingId) {
        DriverReview review = driverReviewService.getReviewForBooking(bookingId);
        if (review == null) {
            return ResponseEntity.ok(Map.of("exists", false));
        }
        return ResponseEntity.ok(review);
    }
    
    @GetMapping("/driver/{driverId}/stats")
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDriverStats(@PathVariable Long driverId) {
        Map<String, Object> stats = driverReviewService.getDriverRatingStats(driverId);
        return ResponseEntity.ok(stats);
    }
    
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        try {
            driverReviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReview(@PathVariable Long reviewId) {
        try {
            DriverReview review = driverReviewService.approveReview(reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectReview(@PathVariable Long reviewId) {
        try {
            DriverReview review = driverReviewService.rejectReview(reviewId);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DriverReview>> getPendingReviews() {
        List<DriverReview> reviews = driverReviewService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
}

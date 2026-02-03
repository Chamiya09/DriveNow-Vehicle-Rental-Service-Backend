package com.drivenow.controller;

import com.drivenow.dto.ReviewDTO;
import com.drivenow.entity.Review;
import com.drivenow.service.ReviewService;
import com.drivenow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    private final NotificationService notificationService;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        try {
            Review review = reviewService.getReviewById(id);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<ReviewDTO>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByVehicleId(@PathVariable Long vehicleId) {
        List<ReviewDTO> reviews = reviewService.getReviewsByVehicleId(vehicleId);
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/approved")
    public ResponseEntity<List<ReviewDTO>> getApprovedReviews() {
        List<ReviewDTO> reviews = reviewService.getReviewsByStatus("APPROVED");
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDTO>> getPendingReviews() {
        List<ReviewDTO> reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDTO>> getReviewsByStatus(@PathVariable String status) {
        try {
            List<ReviewDTO> reviews = reviewService.getReviewsByStatus(status);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReviewDTO>> getReviewsByUserId(@PathVariable Long userId) {
        System.out.println("=== DEBUG: Fetching reviews for user ID: " + userId + " ===");
        List<ReviewDTO> reviews = reviewService.getReviewsByUserId(userId);
        System.out.println("=== DEBUG: Found " + reviews.size() + " reviews ===");
        if (!reviews.isEmpty()) {
            System.out.println("=== DEBUG: First review: " + reviews.get(0).getId() + " - " + reviews.get(0).getVehicleName() + " ===");
        }
        return ResponseEntity.ok(reviews);
    }
    
    @GetMapping("/debug/all")
    public ResponseEntity<?> debugAllReviews() {
        System.out.println("=== DEBUG: Fetching ALL reviews from database ===");
        List<ReviewDTO> allReviews = reviewService.getAllReviews();
        System.out.println("=== DEBUG: Total reviews in database: " + allReviews.size() + " ===");
        
        // Group by user ID
        java.util.Map<Long, Long> reviewsByUser = allReviews.stream()
            .filter(r -> r.getUserId() != null)
            .collect(java.util.stream.Collectors.groupingBy(
                ReviewDTO::getUserId, 
                java.util.stream.Collectors.counting()
            ));
        
        System.out.println("=== DEBUG: Reviews by user: " + reviewsByUser + " ===");
        
        return ResponseEntity.ok(java.util.Map.of(
            "totalReviews", allReviews.size(),
            "reviewsByUser", reviewsByUser,
            "allReviews", allReviews
        ));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReview(@RequestBody java.util.Map<String, Object> request) {
        try {
            System.out.println("=== DEBUG: Received review request: " + request + " ===");
            
            // Extract data from request
            Long userId = null;
            if (request.get("user") instanceof java.util.Map) {
                userId = Long.valueOf(((java.util.Map<?, ?>) request.get("user")).get("id").toString());
            } else if (request.get("userId") != null) {
                userId = Long.valueOf(request.get("userId").toString());
            }
            
            Long vehicleId = null;
            if (request.get("vehicle") instanceof java.util.Map) {
                vehicleId = Long.valueOf(((java.util.Map<?, ?>) request.get("vehicle")).get("id").toString());
            } else if (request.get("vehicleId") != null) {
                vehicleId = Long.valueOf(request.get("vehicleId").toString());
            }
            
            Integer rating = Integer.valueOf(request.get("rating").toString());
            String comment = request.get("comment") != null ? request.get("comment").toString() : "";
            String statusStr = request.get("status") != null ? request.get("status").toString() : "PENDING";
            
            System.out.println("=== DEBUG: Creating review for user " + userId + " and vehicle " + vehicleId + " ===");
            System.out.println("=== DEBUG: Review status: " + statusStr + " ===");
            System.out.println("=== DEBUG: Review rating: " + rating + " ===");
            
            // Create Review entity
            Review review = new Review();
            com.drivenow.entity.User user = new com.drivenow.entity.User();
            user.setId(userId);
            review.setUser(user);
            
            com.drivenow.entity.Vehicle vehicle = new com.drivenow.entity.Vehicle();
            vehicle.setId(vehicleId);
            review.setVehicle(vehicle);
            
            review.setRating(rating);
            review.setComment(comment);
            review.setStatus(Review.ReviewStatus.valueOf(statusStr.toUpperCase()));
            
            Review created = reviewService.createReview(review);
            
            System.out.println("=== DEBUG: Review saved with ID: " + created.getId() + " ===");
            
            // Send review submitted notification
            try {
                if (created.getUser() != null && created.getVehicle() != null) {
                    notificationService.notifyReviewSubmitted(
                        created.getUser(), 
                        created.getVehicle().getName()
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to send review notification: " + e.getMessage());
            }
            
            // Convert to DTO and return
            ReviewDTO dto = ReviewDTO.fromEntity(created);
            System.out.println("=== DEBUG: Returning DTO with userId " + dto.getUserId() + " and vehicleId " + dto.getVehicleId() + " ===");
            
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("=== ERROR: Failed to create review: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Review> updateReviewStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Review updated = reviewService.updateReviewStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

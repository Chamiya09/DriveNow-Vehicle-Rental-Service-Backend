package com.drivenow.service;

import com.drivenow.dto.ReviewDTO;
import com.drivenow.entity.Review;
import com.drivenow.entity.User;
import com.drivenow.entity.Vehicle;
import com.drivenow.repository.ReviewRepository;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleService vehicleService;
    
    public Review getReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
    }
    
    public List<ReviewDTO> getAllReviews() {
        return reviewRepository.findAllWithDetails().stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getReviewsByVehicleId(Long vehicleId) {
        return reviewRepository.findApprovedReviewsByVehicleId(vehicleId).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getReviewsByStatus(String status) {
        return reviewRepository.findByStatusWithDetails(Review.ReviewStatus.valueOf(status.toUpperCase())).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getPendingReviews() {
        return reviewRepository.findByStatusWithDetails(Review.ReviewStatus.PENDING).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    public List<ReviewDTO> getReviewsByUserId(Long userId) {
        System.out.println("=== SERVICE: Querying reviews for user ID: " + userId + " ===");
        List<Review> reviews = reviewRepository.findByUserIdWithDetails(userId);
        System.out.println("=== SERVICE: Query returned " + reviews.size() + " reviews ===");
        
        List<ReviewDTO> dtos = reviews.stream()
                .map(review -> {
                    System.out.println("=== SERVICE: Processing review ID: " + review.getId() + 
                                     ", User: " + (review.getUser() != null ? review.getUser().getId() : "null") +
                                     ", Vehicle: " + (review.getVehicle() != null ? review.getVehicle().getName() : "null") + " ===");
                    return ReviewDTO.fromEntity(review);
                })
                .collect(Collectors.toList());
        
        System.out.println("=== SERVICE: Returning " + dtos.size() + " DTOs ===");
        return dtos;
    }
    
    @Transactional
    public Review createReview(Review review) {
        System.out.println("=== SERVICE: Creating review ===");
        System.out.println("=== SERVICE: Vehicle ID: " + review.getVehicle().getId() + " ===");
        System.out.println("=== SERVICE: User ID: " + (review.getUser() != null ? review.getUser().getId() : "null") + " ===");
        
        Vehicle vehicle = vehicleRepository.findById(review.getVehicle().getId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        System.out.println("=== SERVICE: Vehicle found: " + vehicle.getName() + " ===");
        
        if (review.getUser() != null && review.getUser().getId() != null) {
            User user = userRepository.findById(review.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            review.setUser(user);
            System.out.println("=== SERVICE: User found: " + user.getEmail() + " ===");
        }
        
        review.setVehicle(vehicle);
        
        // Respect the status from the request (auto-approve setting)
        // If no status provided, default to PENDING
        if (review.getStatus() == null) {
            review.setStatus(Review.ReviewStatus.PENDING);
        }
        
        System.out.println("=== SERVICE: Saving review with status: " + review.getStatus() + " ===");
        Review saved = reviewRepository.save(review);
        System.out.println("=== SERVICE: Review saved with ID: " + saved.getId() + " ===");
        
        return saved;
    }
    
    @Transactional
    public Review updateReviewStatus(Long id, String status) {
        Review review = getReviewById(id);
        Review.ReviewStatus newStatus = Review.ReviewStatus.valueOf(status.toUpperCase());
        review.setStatus(newStatus);
        
        Review savedReview = reviewRepository.save(review);
        
        // Update vehicle rating if approved
        if (newStatus == Review.ReviewStatus.APPROVED) {
            vehicleService.updateVehicleRating(review.getVehicle().getId());
        }
        
        return savedReview;
    }
    
    @Transactional
    public void deleteReview(Long id) {
        Review review = getReviewById(id);
        Long vehicleId = review.getVehicle().getId();
        reviewRepository.delete(review);
        
        // Update vehicle rating after deletion
        vehicleService.updateVehicleRating(vehicleId);
    }
}

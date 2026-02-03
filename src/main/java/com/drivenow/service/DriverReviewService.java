package com.drivenow.service;

import com.drivenow.entity.Booking;
import com.drivenow.entity.DriverReview;
import com.drivenow.entity.User;
import com.drivenow.repository.BookingRepository;
import com.drivenow.repository.DriverReviewRepository;
import com.drivenow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DriverReviewService {
    
    private final DriverReviewRepository driverReviewRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    
    @Transactional
    public DriverReview createReview(Long bookingId, Long userId, Integer rating, String comment) {
        // Check if review already exists for this booking
        if (driverReviewRepository.existsByBookingId(bookingId)) {
            throw new RuntimeException("Review already exists for this booking");
        }
        
        // Get booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        // Verify booking is completed
        if (!booking.getStatus().equals(Booking.BookingStatus.COMPLETED)) {
            throw new RuntimeException("Can only review completed bookings");
        }
        
        // Verify user is the customer of this booking
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only review your own bookings");
        }
        
        // Verify booking has a driver
        if (booking.getDriver() == null) {
            throw new RuntimeException("Booking has no assigned driver");
        }
        
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create review
        DriverReview review = new DriverReview();
        review.setDriver(booking.getDriver());
        review.setUser(user);
        review.setBooking(booking);
        review.setRating(rating);
        review.setComment(comment);
        
        return driverReviewRepository.save(review);
    }
    
    public List<DriverReview> getAllReviews() {
        return driverReviewRepository.findAll();
    }
    
    public List<DriverReview> getReviewsForDriver(Long driverId) {
        return driverReviewRepository.findByDriverId(driverId);
    }
    
    public List<DriverReview> getReviewsByUser(Long userId) {
        return driverReviewRepository.findByUserId(userId);
    }
    
    public DriverReview getReviewForBooking(Long bookingId) {
        return driverReviewRepository.findByBookingId(bookingId).orElse(null);
    }
    
    public boolean hasUserReviewedBooking(Long bookingId) {
        return driverReviewRepository.existsByBookingId(bookingId);
    }
    
    public Map<String, Object> getDriverRatingStats(Long driverId) {
        Map<String, Object> stats = new HashMap<>();
        
        Double avgRating = driverReviewRepository.getAverageRatingForDriver(driverId);
        Long reviewCount = driverReviewRepository.getReviewCountForDriver(driverId);
        
        stats.put("averageRating", avgRating != null ? avgRating : 0.0);
        stats.put("totalReviews", reviewCount != null ? reviewCount : 0);
        
        return stats;
    }
    
    @Transactional
    public void deleteReview(Long reviewId) {
        driverReviewRepository.deleteById(reviewId);
    }
    
    @Transactional
    public DriverReview approveReview(Long reviewId) {
        DriverReview review = driverReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(DriverReview.ReviewStatus.APPROVED);
        return driverReviewRepository.save(review);
    }
    
    @Transactional
    public DriverReview rejectReview(Long reviewId) {
        DriverReview review = driverReviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        review.setStatus(DriverReview.ReviewStatus.REJECTED);
        return driverReviewRepository.save(review);
    }
    
    public List<DriverReview> getApprovedReviewsForDriver(Long driverId) {
        return driverReviewRepository.findByDriverId(driverId).stream()
                .filter(review -> review.getStatus() == DriverReview.ReviewStatus.APPROVED)
                .toList();
    }
    
    public List<DriverReview> getPendingReviews() {
        return driverReviewRepository.findAll().stream()
                .filter(review -> review.getStatus() == DriverReview.ReviewStatus.PENDING)
                .toList();
    }
}

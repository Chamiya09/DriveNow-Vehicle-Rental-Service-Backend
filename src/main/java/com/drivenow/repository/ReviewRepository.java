package com.drivenow.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.drivenow.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByVehicleId(Long vehicleId);
    List<Review> findByUserId(Long userId);
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.vehicle WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Review> findByUserIdWithDetails(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.vehicle ORDER BY r.createdAt DESC")
    List<Review> findAllWithDetails();
    
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.vehicle WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Review> findByStatusWithDetails(@Param("status") Review.ReviewStatus status);
    
    List<Review> findByStatus(Review.ReviewStatus status);
    
    void deleteByUserId(Long userId);
    
    @Query("SELECT r FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED' ORDER BY r.createdAt DESC")
    List<Review> findApprovedReviewsByVehicleId(Long vehicleId);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    Double getAverageRatingForVehicle(Long vehicleId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.vehicle.id = :vehicleId AND r.status = 'APPROVED'")
    Long getReviewCountForVehicle(Long vehicleId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId")
    Long countReviewsByUserId(Long userId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND r.createdAt >= DATEADD('MONTH', -1, CURRENT_DATE)")
    Long countReviewsByUserIdLastMonth(Long userId);
    
    @Query("SELECT AVG(dr.rating) FROM DriverReview dr WHERE dr.driver.id = :driverId AND dr.status = 'APPROVED'")
    Double getAverageRatingForDriver(Long driverId);
}

package com.drivenow.repository;

import com.drivenow.entity.DriverReview;
import com.drivenow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverReviewRepository extends JpaRepository<DriverReview, Long> {
    
    @Query("SELECT r FROM DriverReview r WHERE r.driver.id = :driverId")
    List<DriverReview> findByDriverId(@Param("driverId") Long driverId);
    
    @Query("SELECT r FROM DriverReview r WHERE r.user.id = :userId")
    List<DriverReview> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM DriverReview r WHERE r.booking.id = :bookingId")
    Optional<DriverReview> findByBookingId(@Param("bookingId") Long bookingId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM DriverReview r WHERE r.booking.id = :bookingId")
    boolean existsByBookingId(@Param("bookingId") Long bookingId);
    
    @Query("SELECT AVG(r.rating) FROM DriverReview r WHERE r.driver.id = :driverId")
    Double getAverageRatingForDriver(@Param("driverId") Long driverId);
    
    @Query("SELECT COUNT(r) FROM DriverReview r WHERE r.driver.id = :driverId")
    Long getReviewCountForDriver(@Param("driverId") Long driverId);
}

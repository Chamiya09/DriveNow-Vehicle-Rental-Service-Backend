package com.drivenow.repository;

import com.drivenow.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingNumber(String bookingNumber);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId")
    List<Booking> findByUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId")
    List<Booking> findByVehicleId(Long vehicleId);
    
    @Query("SELECT b FROM Booking b WHERE b.driver.id = :driverId")
    List<Booking> findByDriverId(Long driverId);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.driver.id = :driverId ORDER BY b.createdAt DESC")
    List<Booking> findByDriverIdOrderByCreatedAtDesc(Long driverId);
    
    @Query("SELECT b FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND ((b.startDate <= :endDate AND b.endDate >= :startDate)) " +
           "AND b.status NOT IN ('CANCELLED', 'COMPLETED')")
    List<Booking> findConflictingBookings(Long vehicleId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.id = :userId")
    Long countBookingsByUserId(Long userId);
    
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.user.id = :userId AND b.paymentStatus = 'COMPLETED'")
    Double getTotalSpentByUserId(Long userId);
}

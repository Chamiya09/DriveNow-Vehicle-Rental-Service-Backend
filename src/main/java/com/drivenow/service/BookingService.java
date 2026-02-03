package com.drivenow.service;

import com.drivenow.dto.BookingRequest;
import com.drivenow.entity.Booking;
import com.drivenow.entity.User;
import com.drivenow.entity.Vehicle;
import com.drivenow.repository.BookingRepository;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    
    public Booking getBookingByBookingNumber(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }
    
    public List<Booking> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        // Eagerly load user and vehicle to avoid lazy loading issues
        bookings.forEach(booking -> {
            if (booking.getUser() != null) {
                booking.getUser().getName(); // Trigger lazy loading
            }
            if (booking.getVehicle() != null) {
                booking.getVehicle().getName(); // Trigger lazy loading
            }
            if (booking.getDriver() != null) {
                booking.getDriver().getName(); // Trigger lazy loading
            }
        });
        return bookings;
    }
    
    public List<Booking> getBookingsByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
        // Eagerly load relationships
        bookings.forEach(booking -> {
            if (booking.getUser() != null) booking.getUser().getName();
            if (booking.getVehicle() != null) booking.getVehicle().getName();
            if (booking.getDriver() != null) booking.getDriver().getName();
        });
        return bookings;
    }
    
    public List<Booking> getBookingsByDriverId(Long driverId) {
        List<Booking> bookings = bookingRepository.findByDriverIdOrderByCreatedAtDesc(driverId);
        // Eagerly load relationships
        bookings.forEach(booking -> {
            if (booking.getUser() != null) booking.getUser().getName();
            if (booking.getVehicle() != null) booking.getVehicle().getName();
            if (booking.getDriver() != null) booking.getDriver().getName();
        });
        return bookings;
    }
    
    public List<Booking> getBookingsByStatus(String status) {
        return bookingRepository.findByStatus(Booking.BookingStatus.valueOf(status.toUpperCase()));
    }
    
    @Transactional
    public Booking createBooking(BookingRequest bookingRequest) {
        // Validate user exists
        User user = userRepository.findById(bookingRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate vehicle exists and is available
        Vehicle vehicle = vehicleRepository.findById(bookingRequest.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        
        if (!vehicle.getAvailable()) {
            throw new RuntimeException("Vehicle is not available");
        }
        
        // Check for conflicting bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                vehicle.getId(), 
                bookingRequest.getStartDate(), 
                bookingRequest.getEndDate()
        );
        
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Vehicle is already booked for these dates");
        }
        
        // Create booking entity
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setStartDate(bookingRequest.getStartDate());
        booking.setEndDate(bookingRequest.getEndDate());
        booking.setTotalPrice(bookingRequest.getTotalPrice());
        booking.setPickupLocation(bookingRequest.getPickupLocation());
        booking.setPickupLatitude(bookingRequest.getPickupLatitude());
        booking.setPickupLongitude(bookingRequest.getPickupLongitude());
        booking.setDropoffLocation(bookingRequest.getDropoffLocation());
        booking.setDropoffLatitude(bookingRequest.getDropoffLatitude());
        booking.setDropoffLongitude(bookingRequest.getDropoffLongitude());
        booking.setDistanceKm(bookingRequest.getDistanceKm());
        booking.setBasePricePerDay(bookingRequest.getBasePricePerDay());
        booking.setDistancePrice(bookingRequest.getDistancePrice());
        booking.setSpecialRequests(bookingRequest.getSpecialRequests());
        booking.setPaymentMethod(bookingRequest.getPaymentMethod());
        
        // Generate unique booking number
        booking.setBookingNumber(generateBookingNumber());
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setPaymentStatus(Booking.PaymentStatus.PENDING);
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Update vehicle availability
        vehicle.setAvailable(false);
        vehicleRepository.save(vehicle);
        
        return savedBooking;
    }
    
    @Transactional
    public Booking updateBookingStatus(Long id, String status) {
        Booking booking = getBookingById(id);
        Booking.BookingStatus newStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
        booking.setStatus(newStatus);
        
        // If booking is cancelled or completed, make vehicle and driver available
        if (newStatus == Booking.BookingStatus.CANCELLED || 
            newStatus == Booking.BookingStatus.COMPLETED) {
            Vehicle vehicle = booking.getVehicle();
            vehicle.setAvailable(true);
            vehicleRepository.save(vehicle);
            
            // Make driver available again
            if (booking.getDriver() != null) {
                User driver = booking.getDriver();
                driver.setAvailable(true);
                userRepository.save(driver);
            }
        }
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public Booking assignDriver(Long bookingId, Long driverId) {
        Booking booking = getBookingById(bookingId);
        
        // If driverId is null, remove the driver
        if (driverId == null) {
            // Mark current driver as available if one was assigned
            if (booking.getDriver() != null) {
                User currentDriver = booking.getDriver();
                currentDriver.setAvailable(true);
                userRepository.save(currentDriver);
            }
            
            booking.setDriver(null);
            booking.setStatus(Booking.BookingStatus.PENDING);
            return bookingRepository.save(booking);
        }
        
        // Assign new driver
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        if (driver.getRole() != User.Role.DRIVER) {
            throw new RuntimeException("User is not a driver");
        }
        
        if (!driver.getAvailable()) {
            throw new RuntimeException("Driver is not available");
        }
        
        // Mark previous driver as available if one was assigned
        if (booking.getDriver() != null && !booking.getDriver().getId().equals(driverId)) {
            User previousDriver = booking.getDriver();
            previousDriver.setAvailable(true);
            userRepository.save(previousDriver);
        }
        
        booking.setDriver(driver);
        booking.setStatus(Booking.BookingStatus.DRIVER_ASSIGNED);
        
        // Mark driver as unavailable
        driver.setAvailable(false);
        userRepository.save(driver);
        
        return bookingRepository.save(booking);
    }
    
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = getBookingById(id);
        
        // Make vehicle available again
        Vehicle vehicle = booking.getVehicle();
        vehicle.setAvailable(true);
        vehicleRepository.save(vehicle);
        
        // Make driver available if assigned
        if (booking.getDriver() != null) {
            User driver = booking.getDriver();
            driver.setAvailable(true);
            userRepository.save(driver);
        }
        
        bookingRepository.delete(booking);
    }
    
    public Map<String, Object> getBookingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Booking> allBookings = bookingRepository.findAll();
        
        stats.put("totalBookings", allBookings.size());
        stats.put("pendingBookings", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING).count());
        stats.put("confirmedBookings", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED).count());
        stats.put("ongoingBookings", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.ONGOING).count());
        stats.put("completedBookings", allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED).count());
        
        Double totalRevenue = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .mapToDouble(b -> b.getTotalPrice().doubleValue())
                .sum();
        stats.put("totalRevenue", totalRevenue);
        
        return stats;
    }
    
    @Transactional
    public Booking confirmPayment(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        
        // Only allow payment confirmation for completed bookings
        if (booking.getStatus() != Booking.BookingStatus.COMPLETED) {
            throw new RuntimeException("Can only confirm payment for completed bookings");
        }
        
        // Check if payment is already confirmed
        if (booking.getPaymentStatus() == Booking.PaymentStatus.COMPLETED) {
            throw new RuntimeException("Payment already confirmed");
        }
        
        booking.setPaymentStatus(Booking.PaymentStatus.COMPLETED);
        Booking savedBooking = bookingRepository.save(booking);
        
        // Eagerly load relationships for proper JSON response
        if (savedBooking.getUser() != null) savedBooking.getUser().getName();
        if (savedBooking.getVehicle() != null) savedBooking.getVehicle().getName();
        if (savedBooking.getDriver() != null) savedBooking.getDriver().getName();
        
        return savedBooking;
    }
    
    private String generateBookingNumber() {
        String prefix = "BK";
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(9000) + 1000;
        return prefix + timestamp + random;
    }
}

package com.drivenow.controller;

import com.drivenow.entity.Booking;
import com.drivenow.entity.User;
import com.drivenow.service.BookingService;
import com.drivenow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DRIVER')")
public class DriverController {
    
    private final BookingService bookingService;
    private final UserService userService;
    
    @GetMapping("/trips")
    public ResponseEntity<List<Booking>> getDriverTrips(Authentication authentication) {
        try {
            // Get the authenticated driver's email
            String email = authentication.getName();
            
            // Find driver by email
            User driver = userService.findByEmail(email);
            if (driver == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Get all bookings/trips assigned to this driver
            List<Booking> trips = bookingService.getBookingsByDriverId(driver.getId());
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/trips/{tripId}/start")
    public ResponseEntity<?> startTrip(@PathVariable Long tripId, Authentication authentication) {
        try {
            String email = authentication.getName();
            User driver = userService.findByEmail(email);
            
            Booking booking = bookingService.getBookingById(tripId);
            
            // Verify this trip belongs to the authenticated driver
            if (booking.getDriver() == null || !booking.getDriver().getId().equals(driver.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: This trip is not assigned to you");
                return ResponseEntity.status(403).body(error);
            }
            
            // Update booking status to ONGOING
            Booking updatedBooking = bookingService.updateBookingStatus(tripId, "ONGOING");
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to start trip");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/trips/{tripId}/complete")
    public ResponseEntity<?> completeTrip(@PathVariable Long tripId, Authentication authentication) {
        try {
            String email = authentication.getName();
            User driver = userService.findByEmail(email);
            
            Booking booking = bookingService.getBookingById(tripId);
            
            // Verify this trip belongs to the authenticated driver
            if (booking.getDriver() == null || !booking.getDriver().getId().equals(driver.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: This trip is not assigned to you");
                return ResponseEntity.status(403).body(error);
            }
            
            // Update booking status to COMPLETED
            Booking updatedBooking = bookingService.updateBookingStatus(tripId, "COMPLETED");
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to complete trip");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/trips/{tripId}/cancel")
    public ResponseEntity<?> cancelTrip(@PathVariable Long tripId, Authentication authentication) {
        try {
            String email = authentication.getName();
            User driver = userService.findByEmail(email);
            
            Booking booking = bookingService.getBookingById(tripId);
            
            // Verify this trip belongs to the authenticated driver
            if (booking.getDriver() == null || !booking.getDriver().getId().equals(driver.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Unauthorized: This trip is not assigned to you");
                return ResponseEntity.status(403).body(error);
            }
            
            // Update booking status to CANCELLED
            Booking updatedBooking = bookingService.updateBookingStatus(tripId, "CANCELLED");
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to cancel trip");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDriverStats(Authentication authentication) {
        try {
            String email = authentication.getName();
            User driver = userService.findByEmail(email);
            
            if (driver == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Use UserService method to get stats with percentage changes
            Map<String, Object> stats = userService.getDriverStats(driver.getId());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

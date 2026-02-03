package com.drivenow.controller;

import com.drivenow.entity.Booking;
import com.drivenow.entity.User;
import com.drivenow.entity.Vehicle;
import com.drivenow.service.BookingService;
import com.drivenow.service.UserService;
import com.drivenow.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final UserService userService;
    private final VehicleService vehicleService;
    private final BookingService bookingService;
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@RequestParam(required = false) String role) {
        try {
            List<User> users;
            if (role != null && !role.isEmpty()) {
                users = userService.getUsersByRole(role);
            } else {
                users = userService.getAllUsers();
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/drivers")
    public ResponseEntity<List<User>> getAllDrivers() {
        try {
            List<User> drivers = userService.getUsersByRole("DRIVER");
            return ResponseEntity.ok(drivers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete user");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<Map<String, String>> deleteDriver(@PathVariable Long driverId) {
        try {
            userService.deleteUser(driverId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Driver deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete driver");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/vehicles/{vehicleId}")
    public ResponseEntity<Map<String, String>> deleteVehicle(@PathVariable Long vehicleId) {
        try {
            vehicleService.deleteVehicle(vehicleId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Vehicle deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to delete vehicle");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            User user = userService.updateUserStatus(userId, status);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/vehicles/{vehicleId}/availability")
    public ResponseEntity<Vehicle> updateVehicleAvailability(@PathVariable Long vehicleId, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean available = request.get("available");
            Vehicle vehicle = vehicleService.updateVehicleAvailability(vehicleId, available);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/bookings/{bookingId}/assign-driver")
    public ResponseEntity<?> assignDriver(@PathVariable Long bookingId, @RequestBody Map<String, Long> request) {
        try {
            Long driverId = request.get("driverId");
            Booking booking = bookingService.assignDriver(bookingId, driverId);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Failed to assign driver");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @DeleteMapping("/bookings/{bookingId}/remove-driver")
    public ResponseEntity<Booking> removeDriver(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.assignDriver(bookingId, null);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/bookings/{bookingId}/complete")
    public ResponseEntity<Booking> completeBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.updateBookingStatus(bookingId, "COMPLETED");
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.updateBookingStatus(bookingId, "CANCELLED");
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

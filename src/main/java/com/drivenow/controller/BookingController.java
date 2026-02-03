package com.drivenow.controller;

import com.drivenow.dto.BookingRequest;
import com.drivenow.entity.Booking;
import com.drivenow.service.BookingService;
import com.drivenow.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    
    private final BookingService bookingService;
    private final NotificationService notificationService;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        try {
            Booking booking = bookingService.getBookingById(id);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/number/{bookingNumber}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<Booking> getBookingByNumber(@PathVariable String bookingNumber) {
        try {
            Booking booking = bookingService.getBookingByBookingNumber(bookingNumber);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId) {
        List<Booking> bookings = bookingService.getBookingsByUserId(userId);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasAnyRole('DRIVER', 'ADMIN')")
    public ResponseEntity<List<Booking>> getBookingsByDriverId(@PathVariable Long driverId) {
        List<Booking> bookings = bookingService.getBookingsByDriverId(driverId);
        return ResponseEntity.ok(bookings);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@PathVariable String status) {
        try {
            List<Booking> bookings = bookingService.getBookingsByStatus(status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest bookingRequest) {
        try {
            Booking created = bookingService.createBooking(bookingRequest);
            
            // Send notification to user
            try {
                notificationService.notifyBookingCreated(
                    created.getUser(), 
                    created.getId(), 
                    created.getBookingNumber()
                );
            } catch (Exception e) {
                // Log but don't fail booking creation if notification fails
                System.err.println("Failed to send booking created notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            Map<String, String> error = Map.of("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Booking updated = bookingService.updateBookingStatus(id, status);
            
            // Send appropriate notification based on status
            try {
                switch (status.toUpperCase()) {
                    case "CONFIRMED":
                        notificationService.notifyBookingConfirmed(
                            updated.getUser(), 
                            updated.getId(), 
                            updated.getBookingNumber()
                        );
                        break;
                    case "CANCELLED":
                        notificationService.notifyBookingCancelled(
                            updated.getUser(), 
                            updated.getId(), 
                            updated.getBookingNumber()
                        );
                        break;
                    case "COMPLETED":
                        notificationService.notifyBookingCompleted(
                            updated.getUser(), 
                            updated.getId(), 
                            updated.getBookingNumber()
                        );
                        break;
                }
            } catch (Exception e) {
                System.err.println("Failed to send booking status notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{bookingId}/assign-driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Booking> assignDriver(@PathVariable Long bookingId, @PathVariable Long driverId) {
        try {
            Booking updated = bookingService.assignDriver(bookingId, driverId);
            
            // Notify user about driver assignment
            try {
                if (updated.getDriver() != null) {
                    notificationService.notifyDriverAssigned(
                        updated.getUser(), 
                        updated.getId(), 
                        updated.getBookingNumber(),
                        updated.getDriver().getName()
                    );
                }
            } catch (Exception e) {
                System.err.println("Failed to send driver assigned notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/confirm-payment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id) {
        try {
            Booking updated = bookingService.confirmPayment(id);
            
            // Notify user about payment confirmation
            try {
                notificationService.notifyPaymentReceived(
                    updated.getUser(), 
                    updated.getId(), 
                    updated.getTotalPrice()
                );
            } catch (Exception e) {
                System.err.println("Failed to send payment notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> error = Map.of("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        Map<String, Object> stats = bookingService.getBookingStatistics();
        return ResponseEntity.ok(stats);
    }
}

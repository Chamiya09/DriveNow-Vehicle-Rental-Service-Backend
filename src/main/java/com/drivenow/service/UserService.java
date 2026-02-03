package com.drivenow.service;

import com.drivenow.entity.Booking;
import com.drivenow.entity.User;
import com.drivenow.entity.UserSettings;
import com.drivenow.repository.BookingRepository;
import com.drivenow.repository.NotificationRepository;
import com.drivenow.repository.ReviewRepository;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {
    
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final BookingRepository bookingRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User findByEmail(String email) {
        return getUserByEmail(email);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(User.Role.valueOf(role.toUpperCase()));
    }
    
    public List<User> getAllDrivers() {
        return userRepository.findAllActiveDrivers();
    }
    
    public List<User> getAvailableDrivers() {
        return userRepository.findAvailableDrivers();
    }
    
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        if (userDetails.getName() != null) {
            user.setName(userDetails.getName());
        }
        if (userDetails.getPhone() != null) {
            user.setPhone(userDetails.getPhone());
        }
        if (userDetails.getAddress() != null) {
            user.setAddress(userDetails.getAddress());
        }
        if (userDetails.getProfileImage() != null) {
            user.setProfileImage(userDetails.getProfileImage());
        }
        if (userDetails.getLicenseNumber() != null) {
            user.setLicenseNumber(userDetails.getLicenseNumber());
        }
        if (userDetails.getAvailable() != null) {
            user.setAvailable(userDetails.getAvailable());
        }
        
        // Update driver documents
        if (userDetails.getDriversLicense() != null) {
            user.setDriversLicense(userDetails.getDriversLicense());
        }
        if (userDetails.getVehicleRegistration() != null) {
            user.setVehicleRegistration(userDetails.getVehicleRegistration());
        }
        if (userDetails.getInsuranceCertificate() != null) {
            user.setInsuranceCertificate(userDetails.getInsuranceCertificate());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUserStatus(Long id, String status) {
        User user = getUserById(id);
        user.setStatus(User.Status.valueOf(status.toUpperCase()));
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        
        // Check if user is a driver with active bookings
        if (user.getRole() == User.Role.DRIVER) {
            long activeBookings = bookingRepository.findByDriverId(id).stream()
                .filter(booking -> 
                    booking.getStatus() == com.drivenow.entity.Booking.BookingStatus.CONFIRMED ||
                    booking.getStatus() == com.drivenow.entity.Booking.BookingStatus.ONGOING ||
                    booking.getStatus() == com.drivenow.entity.Booking.BookingStatus.DRIVER_ASSIGNED
                )
                .count();
            
            if (activeBookings > 0) {
                throw new RuntimeException("Cannot delete driver with active bookings. Please complete or cancel all active bookings first.");
            }
            
            // Set driver to null in all bookings before deletion (completed or cancelled bookings)
            bookingRepository.findByDriverId(id).forEach(booking -> {
                booking.setDriver(null);
                bookingRepository.save(booking);
            });
        } else {
            // Only check for customer bookings if user is not a driver
            // Check if user has bookings (as a customer)
            long userBookings = bookingRepository.findByUserId(id).size();
            if (userBookings > 0) {
                throw new RuntimeException("Cannot delete user with existing bookings. User has " + userBookings + " booking(s) in the system.");
            }
        }
        
        // Delete all related entities manually
        // 1. Delete user notifications
        notificationRepository.deleteByUserId(id);
        
        // 2. Delete user reviews
        reviewRepository.deleteByUserId(id);
        
        // 3. Delete user settings
        userSettingsRepository.findByUserId(id).ifPresent(userSettings -> {
            userSettingsRepository.delete(userSettings);
        });
        
        // 4. Finally delete the user
        userRepository.delete(user);
    }
    
    @Transactional
    public User updateDriverAvailability(Long driverId, Boolean available) {
        User driver = getUserById(driverId);
        if (driver.getRole() != User.Role.DRIVER) {
            throw new RuntimeException("User is not a driver");
        }
        driver.setAvailable(available);
        return userRepository.save(driver);
    }
    
    public Map<String, Object> getUserStats(Long userId) {
        User user = getUserById(userId);
        Map<String, Object> stats = new HashMap<>();
        
        Long totalBookings = bookingRepository.countBookingsByUserId(userId);
        Double totalSpent = bookingRepository.getTotalSpentByUserId(userId);
        Long activeBookings = getActiveBookingsCount(userId);
        Long reviews = reviewRepository.countReviewsByUserId(userId);
        
        // Calculate percentage changes
        String bookingsChange = calculatePercentageChange(userId, "bookings", false);
        String activeBookingsChange = calculatePercentageChange(userId, "activeBookings", false);
        String spentChange = calculatePercentageChange(userId, "spent", false);
        Long reviewsLastMonth = reviewRepository.countReviewsByUserIdLastMonth(userId);
        String reviewsChange = reviewsLastMonth > 0 ? "+" + reviewsLastMonth : "0";
        
        stats.put("userId", user.getId());
        stats.put("name", user.getName());
        stats.put("email", user.getEmail());
        stats.put("phone", user.getPhone());
        stats.put("address", user.getAddress());
        stats.put("role", user.getRole().name());
        stats.put("status", user.getStatus().name());
        stats.put("profileImage", user.getProfileImage());
        stats.put("licenseNumber", user.getLicenseNumber());
        stats.put("available", user.getAvailable());
        stats.put("joinDate", user.getCreatedAt());
        stats.put("totalBookings", totalBookings);
        stats.put("activeBookings", activeBookings);
        stats.put("totalSpent", totalSpent);
        stats.put("reviews", reviews);
        
        // Percentage changes
        stats.put("bookingsChange", bookingsChange);
        stats.put("activeBookingsChange", activeBookingsChange);
        stats.put("spentChange", spentChange);
        stats.put("reviewsChange", reviewsChange);
        
        // Include driver documents
        stats.put("driversLicense", user.getDriversLicense());
        stats.put("vehicleRegistration", user.getVehicleRegistration());
        stats.put("insuranceCertificate", user.getInsuranceCertificate());
        
        return stats;
    }
    
    private Long getActiveBookingsCount(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING || 
                        b.getStatus() == Booking.BookingStatus.CONFIRMED)
            .count();
    }
    
    private String calculatePercentageChange(Long userId, String type, boolean isDriver) {
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.withDayOfMonth(1).minusDays(1);
        LocalDate prevMonthStart = lastMonthStart.minusMonths(1);
        LocalDate prevMonthEnd = lastMonthStart.minusDays(1);
        
        List<Booking> allBookings = isDriver ? 
            bookingRepository.findByDriverId(userId) : 
            bookingRepository.findByUserId(userId);
        
        if (type.equals("bookings")) {
            long lastMonth = allBookings.stream()
                .filter(b -> !b.getCreatedAt().toLocalDate().isBefore(lastMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(lastMonthEnd))
                .count();
            long prevMonth = allBookings.stream()
                .filter(b -> !b.getCreatedAt().toLocalDate().isBefore(prevMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(prevMonthEnd))
                .count();
            return calculatePercentage(lastMonth, prevMonth);
        } else if (type.equals("activeBookings")) {
            long lastMonth = allBookings.stream()
                .filter(b -> (b.getStatus() == Booking.BookingStatus.PENDING || 
                             b.getStatus() == Booking.BookingStatus.CONFIRMED) &&
                            !b.getCreatedAt().toLocalDate().isBefore(lastMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(lastMonthEnd))
                .count();
            long prevMonth = allBookings.stream()
                .filter(b -> (b.getStatus() == Booking.BookingStatus.PENDING || 
                             b.getStatus() == Booking.BookingStatus.CONFIRMED) &&
                            !b.getCreatedAt().toLocalDate().isBefore(prevMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(prevMonthEnd))
                .count();
            return calculatePercentage(lastMonth, prevMonth);
        } else if (type.equals("spent") || type.equals("earnings")) {
            double lastMonth = allBookings.stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.COMPLETED &&
                            !b.getCreatedAt().toLocalDate().isBefore(lastMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(lastMonthEnd))
                .mapToDouble(b -> isDriver ? b.getTotalPrice() * 0.15 : b.getTotalPrice())
                .sum();
            double prevMonth = allBookings.stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.COMPLETED &&
                            !b.getCreatedAt().toLocalDate().isBefore(prevMonthStart) && 
                            !b.getCreatedAt().toLocalDate().isAfter(prevMonthEnd))
                .mapToDouble(b -> isDriver ? b.getTotalPrice() * 0.15 : b.getTotalPrice())
                .sum();
            return calculatePercentage((long)lastMonth, (long)prevMonth);
        }
        return "+0%";
    }
    
    private String calculatePercentage(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double change = ((double)(current - previous) / previous) * 100;
        if (change > 0) {
            return "+" + Math.round(change) + "%";
        } else if (change < 0) {
            return Math.round(change) + "%";
        }
        return "0%";
    }
    
    public Map<String, Object> getDriverStats(Long driverId) {
        User driver = getUserById(driverId);
        Map<String, Object> stats = new HashMap<>();
        
        List<Booking> trips = bookingRepository.findByDriverId(driverId);
        
        long totalTrips = trips.size();
        long completedTrips = trips.stream()
                .filter(t -> t.getStatus() == Booking.BookingStatus.COMPLETED)
                .count();
        long activeTrips = trips.stream()
                .filter(t -> t.getStatus() == Booking.BookingStatus.CONFIRMED || 
                           t.getStatus() == Booking.BookingStatus.ONGOING ||
                           t.getStatus() == Booking.BookingStatus.DRIVER_ASSIGNED)
                .count();
        double totalEarnings = trips.stream()
                .filter(t -> t.getStatus() == Booking.BookingStatus.COMPLETED &&
                           t.getPaymentStatus() == Booking.PaymentStatus.COMPLETED)
                .mapToDouble(t -> t.getTotalPrice() * 0.15)
                .sum();
        Double averageRating = reviewRepository.getAverageRatingForDriver(driverId);
        
        // Calculate percentage changes
        String tripsChange = calculatePercentageChange(driverId, "bookings", true);
        String activeTripsChange = calculatePercentageChange(driverId, "activeBookings", true);
        String earningsChange = calculatePercentageChange(driverId, "earnings", true);
        
        stats.put("userId", driver.getId());
        stats.put("name", driver.getName());
        stats.put("email", driver.getEmail());
        stats.put("totalTrips", totalTrips);
        stats.put("completedTrips", completedTrips);
        stats.put("activeTrips", activeTrips);
        stats.put("totalEarnings", totalEarnings);
        stats.put("averageRating", averageRating != null ? averageRating : 0.0);
        stats.put("commissionRate", 0.15);
        
        // Percentage changes
        stats.put("tripsChange", tripsChange);
        stats.put("activeTripsChange", activeTripsChange);
        stats.put("earningsChange", earningsChange);
        stats.put("ratingChange", "+0.2");
        
        return stats;
    }
    
    public UserSettings getUserSettings(Long userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Settings not found"));
    }
    
    @Transactional
    public UserSettings updateUserSettings(Long userId, UserSettings newSettings) {
        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings s = new UserSettings();
                    s.setUser(getUserById(userId));
                    return s;
                });
        
        if (newSettings.getEmailNotifications() != null) {
            settings.setEmailNotifications(newSettings.getEmailNotifications());
        }
        if (newSettings.getSmsNotifications() != null) {
            settings.setSmsNotifications(newSettings.getSmsNotifications());
        }
        if (newSettings.getPromotionalEmails() != null) {
            settings.setPromotionalEmails(newSettings.getPromotionalEmails());
        }
        if (newSettings.getBookingReminders() != null) {
            settings.setBookingReminders(newSettings.getBookingReminders());
        }
        if (newSettings.getVehicleAvailabilityAlerts() != null) {
            settings.setVehicleAvailabilityAlerts(newSettings.getVehicleAvailabilityAlerts());
        }
        if (newSettings.getShowBookingHistory() != null) {
            settings.setShowBookingHistory(newSettings.getShowBookingHistory());
        }
        if (newSettings.getAutoRenewBookings() != null) {
            settings.setAutoRenewBookings(newSettings.getAutoRenewBookings());
        }
        if (newSettings.getPreferredPickupTime() != null) {
            settings.setPreferredPickupTime(newSettings.getPreferredPickupTime());
        }
        if (newSettings.getTheme() != null) {
            settings.setTheme(newSettings.getTheme());
        }
        
        return userSettingsRepository.save(settings);
    }
    
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = getUserById(userId);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Update password with encoding
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    @Transactional
    public User updateProfileImage(Long userId, String imageUrl) {
        User user = getUserById(userId);
        user.setProfileImage(imageUrl);
        return userRepository.save(user);
    }
}

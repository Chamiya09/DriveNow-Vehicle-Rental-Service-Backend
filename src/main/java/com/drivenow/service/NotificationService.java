package com.drivenow.service;

import com.drivenow.entity.Notification;
import com.drivenow.entity.User;
import com.drivenow.repository.NotificationRepository;
import com.drivenow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    /**
     * Verify that the authenticated user has access to the specified user's data
     * Users can only access their own data unless they are ADMIN
     */
    private void verifyUserAccess(Long requestedUserId, String authenticatedEmail) {
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        
        // Admins can access all notifications
        if (User.Role.ADMIN.equals(authenticatedUser.getRole())) {
            return;
        }
        
        // Other users (including drivers) can only access their own notifications
        if (!Objects.equals(authenticatedUser.getId(), requestedUserId)) {
            throw new SecurityException("Access denied: You can only access your own notifications");
        }
    }
    
    /**
     * Sanitize notification content to prevent XSS attacks
     */
    private void sanitizeNotification(Notification notification) {
        if (notification.getTitle() != null) {
            notification.setTitle(HtmlUtils.htmlEscape(notification.getTitle().trim()));
        }
        if (notification.getMessage() != null) {
            notification.setMessage(HtmlUtils.htmlEscape(notification.getMessage().trim()));
        }
        if (notification.getActionUrl() != null) {
            notification.setActionUrl(HtmlUtils.htmlEscape(notification.getActionUrl().trim()));
        }
    }
    
    /**
     * Validate notification input
     */
    private void validateNotification(Notification notification) {
        if (notification.getTitle() == null || notification.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification title is required");
        }
        if (notification.getMessage() == null || notification.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Notification message is required");
        }
        if (notification.getTitle().length() > 255) {
            throw new IllegalArgumentException("Notification title is too long (max 255 characters)");
        }
        if (notification.getMessage().length() > 1000) {
            throw new IllegalArgumentException("Notification message is too long (max 1000 characters)");
        }
    }
    
    public Notification getNotificationById(Long id, String authenticatedEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Verify user has access to this notification
        verifyUserAccess(notification.getUser().getId(), authenticatedEmail);
        
        return notification;
    }
    
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
    
    public List<Notification> getNotificationsByUserId(Long userId, String authenticatedEmail) {
        verifyUserAccess(userId, authenticatedEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public Long getUnreadCount(Long userId, String authenticatedEmail) {
        verifyUserAccess(userId, authenticatedEmail);
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    @Transactional
    public Notification createNotification(Notification notification, String authenticatedEmail) {
        // Validate input
        validateNotification(notification);
        
        // Sanitize content to prevent XSS
        sanitizeNotification(notification);
        
        Long userId = notification.getUserId();
        if (userId == null && notification.getUser() != null) {
            userId = notification.getUser().getId();
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        // Verify user has permission to create notification for this user
        verifyUserAccess(userId, authenticatedEmail);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        notification.setUser(user);
        notification.setIsRead(false);
        
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public Notification markAsRead(Long id, String authenticatedEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Verify user has access to this notification
        verifyUserAccess(notification.getUser().getId(), authenticatedEmail);
        
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId, String authenticatedEmail) {
        verifyUserAccess(userId, authenticatedEmail);
        List<Notification> notifications = notificationRepository.findByUserIdAndIsRead(userId, false);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }
    
    @Transactional
    public void deleteNotification(Long id, String authenticatedEmail) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        // Verify user has access to this notification
        verifyUserAccess(notification.getUser().getId(), authenticatedEmail);
        
        notificationRepository.delete(notification);
    }
    
    @Transactional
    public void clearAllNotifications(Long userId, String authenticatedEmail) {
        verifyUserAccess(userId, authenticatedEmail);
        notificationRepository.deleteByUserId(userId);
    }
    
    /**
     * Create a system notification without authentication check
     * Used for automated system-generated notifications (e.g., contact messages)
     */
    @Transactional
    public Notification createSystemNotification(Notification notification) {
        // Validate input
        validateNotification(notification);
        
        // Sanitize content to prevent XSS
        sanitizeNotification(notification);
        
        // Ensure user is set
        if (notification.getUser() == null) {
            throw new IllegalArgumentException("User is required for notification");
        }
        
        notification.setIsRead(false);
        
        return notificationRepository.save(notification);
    }
    
    // ==================== BOOKING NOTIFICATIONS ====================
    
    @Transactional
    public void notifyBookingCreated(User user, Long bookingId, String bookingNumber) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Booking Created Successfully");
        notification.setMessage("Your booking " + bookingNumber + " has been created successfully. Awaiting confirmation.");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.BOOKING);
        notification.setBookingId(bookingId);
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyBookingConfirmed(User user, Long bookingId, String bookingNumber) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Booking Confirmed");
        notification.setMessage("Great news! Your booking " + bookingNumber + " has been confirmed.");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.BOOKING);
        notification.setBookingId(bookingId);
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyBookingCancelled(User user, Long bookingId, String bookingNumber) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Booking Cancelled");
        notification.setMessage("Your booking " + bookingNumber + " has been cancelled.");
        notification.setType(Notification.NotificationType.WARNING);
        notification.setCategory(Notification.NotificationCategory.BOOKING);
        notification.setBookingId(bookingId);
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyBookingCompleted(User user, Long bookingId, String bookingNumber) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Booking Completed");
        notification.setMessage("Your booking " + bookingNumber + " has been completed. Thank you for choosing us!");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.BOOKING);
        notification.setBookingId(bookingId);
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyDriverAssigned(User user, Long bookingId, String bookingNumber, String driverName) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Driver Assigned");
        notification.setMessage("Driver " + driverName + " has been assigned to your booking " + bookingNumber + ".");
        notification.setType(Notification.NotificationType.INFO);
        notification.setCategory(Notification.NotificationCategory.BOOKING);
        notification.setBookingId(bookingId);
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    // ==================== MESSAGE NOTIFICATIONS ====================
    
    @Transactional
    public void notifyNewMessage(User user, String subject) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("New Message Received");
        notification.setMessage("You have received a new message regarding: " + subject);
        notification.setType(Notification.NotificationType.INFO);
        notification.setCategory(Notification.NotificationCategory.MESSAGE);
        notification.setActionUrl("/dashboard/user?tab=messages");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyMessageReply(User user, String subject) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Reply to Your Message");
        notification.setMessage("You have received a reply to your message: " + subject);
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.MESSAGE);
        notification.setActionUrl("/dashboard/user?tab=messages");
        createSystemNotification(notification);
    }
    
    // ==================== PAYMENT NOTIFICATIONS ====================
    
    @Transactional
    public void notifyPaymentReceived(User user, Long bookingId, BigDecimal amount) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Payment Received");
        notification.setMessage("Payment of $" + String.format("%.2f", amount) + " has been received successfully.");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.PAYMENT);
        notification.setBookingId(bookingId);
        notification.setAmount(amount.doubleValue());
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyPaymentPending(User user, Long bookingId, BigDecimal amount) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Payment Pending");
        notification.setMessage("Payment of $" + String.format("%.2f", amount) + " is pending. Please complete the payment.");
        notification.setType(Notification.NotificationType.WARNING);
        notification.setCategory(Notification.NotificationCategory.PAYMENT);
        notification.setBookingId(bookingId);
        notification.setAmount(amount.doubleValue());
        notification.setActionUrl("/dashboard/user?tab=bookings");
        createSystemNotification(notification);
    }
    
    // ==================== REVIEW NOTIFICATIONS ====================
    
    @Transactional
    public void notifyReviewSubmitted(User user, String vehicleName) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Review Submitted");
        notification.setMessage("Thank you for reviewing " + vehicleName + ". Your feedback helps us improve!");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.REVIEW);
        notification.setActionUrl("/dashboard/user?tab=reviews");
        createSystemNotification(notification);
    }
    
    // ==================== VEHICLE NOTIFICATIONS ====================
    
    @Transactional
    public void notifyVehicleAvailable(User user, String vehicleName) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Vehicle Now Available");
        notification.setMessage("The vehicle " + vehicleName + " you were interested in is now available for booking.");
        notification.setType(Notification.NotificationType.INFO);
        notification.setCategory(Notification.NotificationCategory.SYSTEM);
        notification.setActionUrl("/vehicles");
        createSystemNotification(notification);
    }
    
    // ==================== SYSTEM NOTIFICATIONS ====================
    
    @Transactional
    public void notifyAccountCreated(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Welcome to DriveNow!");
        notification.setMessage("Your account has been created successfully. Start exploring our vehicles!");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.SYSTEM);
        notification.setActionUrl("/vehicles");
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyProfileUpdated(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Profile Updated");
        notification.setMessage("Your profile has been updated successfully.");
        notification.setType(Notification.NotificationType.SUCCESS);
        notification.setCategory(Notification.NotificationCategory.SYSTEM);
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyPasswordChanged(User user) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Password Changed");
        notification.setMessage("Your password has been changed successfully. If you didn't make this change, please contact support immediately.");
        notification.setType(Notification.NotificationType.WARNING);
        notification.setCategory(Notification.NotificationCategory.SYSTEM);
        createSystemNotification(notification);
    }
    
    @Transactional
    public void notifyAdminAction(User user, String action, String details) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle("Admin Action: " + action);
        notification.setMessage(details);
        notification.setType(Notification.NotificationType.INFO);
        notification.setCategory(Notification.NotificationCategory.SYSTEM);
        createSystemNotification(notification);
    }
}

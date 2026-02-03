package com.drivenow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @Transient
    @JsonProperty("userId")
    private Long userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.INFO;
    
    @Enumerated(EnumType.STRING)
    private NotificationCategory category;
    
    @Column(nullable = false)
    private Boolean isRead = false;
    
    private String actionUrl;
    
    private Long bookingId;
    private Long tripId;
    private Long vehicleId;
    private Long customerId;
    private Long driverId;
    private Double amount;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PostLoad
    public void populateUserId() {
        if (this.user != null) {
            this.userId = this.user.getId();
        }
    }
    
    public enum NotificationType {
        INFO, SUCCESS, WARNING, ERROR, URGENT
    }
    
    public enum NotificationCategory {
        BOOKING, PAYMENT, TRIP, REVIEW, SYSTEM, MESSAGE, VEHICLE, ACCOUNT, DRIVER
    }
}

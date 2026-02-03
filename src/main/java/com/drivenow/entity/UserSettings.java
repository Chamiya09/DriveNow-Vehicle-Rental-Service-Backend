package com.drivenow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;
    
    @Column(nullable = false)
    private Boolean emailNotifications = true;
    
    @Column(nullable = false)
    private Boolean smsNotifications = true;
    
    @Column(nullable = false)
    private Boolean promotionalEmails = false;
    
    @Column(nullable = false)
    private Boolean bookingReminders = true;
    
    @Column(nullable = false)
    private Boolean vehicleAvailabilityAlerts = false;
    
    @Column(nullable = false)
    private Boolean showBookingHistory = false;
    
    @Column(nullable = false)
    private Boolean autoRenewBookings = false;
    
    @Column(nullable = false)
    private String preferredPickupTime = "09:00";
    
    @Column(nullable = false)
    private String theme = "light";
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

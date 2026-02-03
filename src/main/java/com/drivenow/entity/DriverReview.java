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
@Table(name = "driver_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DriverReview {
    
    public enum ReviewStatus {
        PENDING, APPROVED, REJECTED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @JsonIgnore
    private User driver;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;
    
    @Column(nullable = false)
    private Integer rating; // 1-5
    
    @Column(length = 1000)
    private String comment;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PENDING;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Expose IDs for JSON
    @JsonProperty("driverId")
    public Long getDriverId() {
        return driver != null ? driver.getId() : null;
    }
    
    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return booking != null ? booking.getId() : null;
    }
    
    // Expose driver name
    @JsonProperty("driverName")
    public String getDriverName() {
        return driver != null ? driver.getName() : null;
    }
    
    // Expose user name
    @JsonProperty("userName")
    public String getUserName() {
        return user != null ? user.getName() : null;
    }
    
    // Expose user email
    @JsonProperty("userEmail")
    public String getUserEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    // Expose booking number
    @JsonProperty("bookingNumber")
    public String getBookingNumber() {
        return booking != null ? booking.getBookingNumber() : null;
    }
}

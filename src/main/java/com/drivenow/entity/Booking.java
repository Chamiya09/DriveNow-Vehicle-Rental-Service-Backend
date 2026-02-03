package com.drivenow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String bookingNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnore
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonIgnore
    private User driver;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;
    
    @Column(nullable = false)
    private String pickupLocation;
    
    private Double pickupLatitude;
    
    private Double pickupLongitude;
    
    @Column(nullable = false)
    private String dropoffLocation;
    
    private Double dropoffLatitude;
    
    private Double dropoffLongitude;
    
    private Double distanceKm;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal basePricePerDay;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal distancePrice;
    
    @Column(length = 1000)
    private String specialRequests;
    
    private String paymentMethod;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    // Helper methods to expose IDs for JSON serialization
    @JsonProperty("userId")
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    @JsonProperty("vehicleId")
    public Long getVehicleId() {
        return vehicle != null ? vehicle.getId() : null;
    }
    
    @JsonProperty("driverId")
    public Long getDriverId() {
        return driver != null ? driver.getId() : null;
    }
    
    // Expose user details for frontend (safe for drivers to see customer contact)
    @JsonProperty("userInfo")
    public Map<String, Object> getUserInfo() {
        if (user == null) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("name", user.getName());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone()); // Add phone for driver contact
        return info;
    }
    
    // Expose vehicle details for frontend
    @JsonProperty("vehicleInfo")
    public Map<String, Object> getVehicleInfo() {
        if (vehicle == null) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("id", vehicle.getId());
        info.put("name", vehicle.getName());
        info.put("category", vehicle.getCategory());
        return info;
    }
    
    // Expose customer name for driver dashboard
    @JsonProperty("customerName")
    public String getCustomerName() {
        return user != null ? user.getName() : null;
    }
    
    // Expose vehicle name for driver dashboard
    @JsonProperty("vehicleName")
    public String getVehicleName() {
        return vehicle != null ? vehicle.getName() : null;
    }
    
    // Expose driver details for user dashboard
    @JsonProperty("driverInfo")
    public Map<String, Object> getDriverInfo() {
        if (driver == null) return null;
        Map<String, Object> info = new HashMap<>();
        info.put("id", driver.getId());
        info.put("name", driver.getName());
        info.put("email", driver.getEmail());
        info.put("phone", driver.getPhone());
        info.put("licenseNumber", driver.getLicenseNumber());
        return info;
    }
    
    // Expose driver name for easy access
    @JsonProperty("driverName")
    public String getDriverName() {
        return driver != null ? driver.getName() : null;
    }
    
    public enum BookingStatus {
        PENDING, CONFIRMED, ONGOING, COMPLETED, CANCELLED, DRIVER_ASSIGNED
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
}

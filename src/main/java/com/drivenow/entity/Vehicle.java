package com.drivenow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleCategory category;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKm = BigDecimal.valueOf(2.00); // Default $2 per km
    
    private String image;
    
    @Column(nullable = false)
    private Double rating = 0.0;
    
    @Column(nullable = false)
    private Integer reviewCount = 0;
    
    @Column(nullable = false)
    private Integer seats;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransmissionType transmission;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FuelType fuelType;
    
    @ElementCollection
    @CollectionTable(name = "vehicle_features", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "feature")
    private Set<String> features = new HashSet<>();
    
    @Column(nullable = false)
    private Boolean available = true;
    
    @Column(length = 1000)
    private String description;
    
    private String licensePlate;
    
    @Column(name = "\"year\"")  // Escape reserved keyword for H2
    private Integer year;
    
    private String color;
    
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Booking> bookings = new HashSet<>();
    
    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Review> reviews = new HashSet<>();
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    public enum VehicleCategory {
        SUV, SEDAN, HATCHBACK, VAN, LUXURY
    }
    
    public enum TransmissionType {
        AUTOMATIC, MANUAL
    }
    
    public enum FuelType {
        PETROL, DIESEL, ELECTRIC, HYBRID
    }
}

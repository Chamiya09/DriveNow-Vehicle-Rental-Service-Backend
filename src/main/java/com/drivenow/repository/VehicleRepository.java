package com.drivenow.repository;

import com.drivenow.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByCategory(Vehicle.VehicleCategory category);
    List<Vehicle> findByAvailable(Boolean available);
    List<Vehicle> findByAvailableTrue();
    
    @Query("SELECT v FROM Vehicle v WHERE v.pricePerDay BETWEEN :minPrice AND :maxPrice")
    List<Vehicle> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
    
    @Query("SELECT v FROM Vehicle v WHERE v.available = true AND v.category = :category")
    List<Vehicle> findAvailableByCategory(Vehicle.VehicleCategory category);
    
    @Query("SELECT v FROM Vehicle v ORDER BY v.rating DESC")
    List<Vehicle> findTopRated();
}

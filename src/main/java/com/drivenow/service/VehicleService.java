package com.drivenow.service;

import com.drivenow.entity.Vehicle;
import com.drivenow.repository.ReviewRepository;
import com.drivenow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final ReviewRepository reviewRepository;
    
    public Vehicle getVehicleById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        // Update rating from reviews
        updateVehicleRatingFromReviews(vehicle);
        return vehicle;
    }
    
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        // Update ratings for all vehicles from reviews
        vehicles.forEach(this::updateVehicleRatingFromReviews);
        return vehicles;
    }
    
    private void updateVehicleRatingFromReviews(Vehicle vehicle) {
        Double averageRating = reviewRepository.getAverageRatingForVehicle(vehicle.getId());
        Long reviewCount = reviewRepository.getReviewCountForVehicle(vehicle.getId());
        vehicle.setRating(averageRating != null ? averageRating : 0.0);
        vehicle.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);
    }
    
    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findByAvailableTrue();
        // Update ratings for all vehicles from reviews
        vehicles.forEach(this::updateVehicleRatingFromReviews);
        return vehicles;
    }
    
    public List<Vehicle> getVehiclesByCategory(String category) {
        List<Vehicle> vehicles = vehicleRepository.findByCategory(Vehicle.VehicleCategory.valueOf(category.toUpperCase()));
        // Update ratings for all vehicles from reviews
        vehicles.forEach(this::updateVehicleRatingFromReviews);
        return vehicles;
    }
    
    public List<Vehicle> getVehiclesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<Vehicle> vehicles = vehicleRepository.findByPriceRange(minPrice, maxPrice);
        // Update ratings for all vehicles from reviews
        vehicles.forEach(this::updateVehicleRatingFromReviews);
        return vehicles;
    }
    
    @Transactional
    public Vehicle createVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }
    
    @Transactional
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);
        
        if (vehicleDetails.getName() != null) {
            vehicle.setName(vehicleDetails.getName());
        }
        if (vehicleDetails.getCategory() != null) {
            vehicle.setCategory(vehicleDetails.getCategory());
        }
        if (vehicleDetails.getPricePerDay() != null) {
            vehicle.setPricePerDay(vehicleDetails.getPricePerDay());
        }
        if (vehicleDetails.getImage() != null) {
            vehicle.setImage(vehicleDetails.getImage());
        }
        if (vehicleDetails.getSeats() != null) {
            vehicle.setSeats(vehicleDetails.getSeats());
        }
        if (vehicleDetails.getTransmission() != null) {
            vehicle.setTransmission(vehicleDetails.getTransmission());
        }
        if (vehicleDetails.getFuelType() != null) {
            vehicle.setFuelType(vehicleDetails.getFuelType());
        }
        if (vehicleDetails.getFeatures() != null) {
            vehicle.setFeatures(vehicleDetails.getFeatures());
        }
        if (vehicleDetails.getAvailable() != null) {
            vehicle.setAvailable(vehicleDetails.getAvailable());
        }
        if (vehicleDetails.getDescription() != null) {
            vehicle.setDescription(vehicleDetails.getDescription());
        }
        if (vehicleDetails.getLicensePlate() != null) {
            vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        }
        if (vehicleDetails.getYear() != null) {
            vehicle.setYear(vehicleDetails.getYear());
        }
        if (vehicleDetails.getColor() != null) {
            vehicle.setColor(vehicleDetails.getColor());
        }
        
        return vehicleRepository.save(vehicle);
    }
    
    @Transactional
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicleRepository.delete(vehicle);
    }
    
    @Transactional
    public Vehicle updateVehicleAvailability(Long id, Boolean available) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setAvailable(available);
        return vehicleRepository.save(vehicle);
    }
    
    @Transactional
    public void updateVehicleRating(Long vehicleId) {
        Vehicle vehicle = getVehicleById(vehicleId);
        
        Double averageRating = reviewRepository.getAverageRatingForVehicle(vehicleId);
        Long reviewCount = reviewRepository.getReviewCountForVehicle(vehicleId);
        
        vehicle.setRating(averageRating != null ? averageRating : 0.0);
        vehicle.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);
        
        vehicleRepository.save(vehicle);
    }
}

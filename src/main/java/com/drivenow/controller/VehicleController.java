package com.drivenow.controller;

import com.drivenow.entity.Vehicle;
import com.drivenow.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(id);
            return ResponseEntity.ok(vehicle);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        List<Vehicle> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Vehicle>> getVehiclesByCategory(@PathVariable String category) {
        try {
            List<Vehicle> vehicles = vehicleService.getVehiclesByCategory(category);
            return ResponseEntity.ok(vehicles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<Vehicle>> getVehiclesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(vehicles);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle created = vehicleService.createVehicle(vehicle);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vehicle> updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        try {
            Vehicle updated = vehicleService.updateVehicle(id, vehicle);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Vehicle> updateVehicleAvailability(@PathVariable Long id, @RequestParam Boolean available) {
        try {
            Vehicle updated = vehicleService.updateVehicleAvailability(id, available);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        try {
            vehicleService.deleteVehicle(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

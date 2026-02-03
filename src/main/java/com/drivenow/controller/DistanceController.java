package com.drivenow.controller;

import com.drivenow.service.DistanceCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/distance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class DistanceController {
    
    private final DistanceCalculationService distanceService;
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateDistance(@RequestBody Map<String, Double> request) {
        System.out.println("🔵 ===== Distance Calculation Request =====");
        System.out.println("📥 Request payload: " + request);
        
        try {
            Double lat1 = request.get("lat1");
            Double lon1 = request.get("lon1");
            Double lat2 = request.get("lat2");
            Double lon2 = request.get("lon2");
            
            System.out.println("📍 Coordinates:");
            System.out.println("   lat1=" + lat1 + ", lon1=" + lon1);
            System.out.println("   lat2=" + lat2 + ", lon2=" + lon2);
            
            if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
                System.err.println("❌ Missing coordinates in request");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Missing coordinates", "received", request));
            }
            
            // Validate coordinate ranges
            if (lat1 < -90 || lat1 > 90 || lat2 < -90 || lat2 > 90) {
                System.err.println("❌ Invalid latitude values");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Latitude must be between -90 and 90"));
            }
            
            if (lon1 < -180 || lon1 > 180 || lon2 < -180 || lon2 > 180) {
                System.err.println("❌ Invalid longitude values");
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Longitude must be between -180 and 180"));
            }
            
            double distance = distanceService.calculateDistance(lat1, lon1, lat2, lon2);
            
            if (distance <= 0) {
                System.err.println("⚠️ Calculated distance is zero or negative: " + distance);
                return ResponseEntity.ok(Map.of(
                    "distanceKm", 0.0,
                    "distanceMiles", 0.0,
                    "warning", "Could not calculate valid distance"
                ));
            }
            
            Map<String, Object> response = Map.of(
                "distanceKm", distance,
                "distanceMiles", Math.round(distance * 0.621371 * 100.0) / 100.0
            );
            
            System.out.println("📤 Success! Returning: " + response);
            System.out.println("🔵 ==========================================");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("❌ FATAL ERROR in distance calculation:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Type: " + e.getClass().getName());
            e.printStackTrace();
            System.out.println("🔵 ==========================================");
            
            return ResponseEntity.status(500)
                .body(Map.of(
                    "error", "Internal server error: " + e.getMessage(),
                    "type", e.getClass().getSimpleName()
                ));
        }
    }
}

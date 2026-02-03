package com.drivenow.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DistanceCalculationService {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Calculate real road distance between two coordinates using OSRM (OpenStreetMap Routing)
     * Falls back to Haversine formula if OSRM service is unavailable
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        System.out.println("📍 Distance calculation requested");
        System.out.println("   From: (" + lat1 + ", " + lon1 + ")");
        System.out.println("   To: (" + lat2 + ", " + lon2 + ")");
        
        try {
            // Try to get real road distance from OSRM (OpenStreetMap Routing Machine)
            String osrmUrl = String.format(
                "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                lon1, lat1, lon2, lat2
            );
            
            System.out.println("🗺️ Requesting route from OSRM...");
            System.out.println("   URL: " + osrmUrl);
            
            String response = restTemplate.getForObject(osrmUrl, String.class);
            
            if (response != null && !response.isEmpty()) {
                JsonNode root = objectMapper.readTree(response);
                
                if (root.has("code") && "Ok".equals(root.get("code").asText())) {
                    if (root.has("routes") && root.get("routes").size() > 0) {
                        // Distance is in meters, convert to kilometers
                        double distanceMeters = root.get("routes").get(0).get("distance").asDouble();
                        double distanceKm = distanceMeters / 1000.0;
                        double roundedDistance = Math.round(distanceKm * 100.0) / 100.0;
                        
                        System.out.println("✅ OSRM road distance: " + roundedDistance + " km");
                        return roundedDistance;
                    }
                }
                System.out.println("⚠️ OSRM returned invalid response, using fallback");
            }
        } catch (RestClientException e) {
            System.err.println("⚠️ OSRM API connection failed: " + e.getMessage());
            System.err.println("   Falling back to Haversine formula");
        } catch (Exception e) {
            System.err.println("⚠️ Error parsing OSRM response: " + e.getMessage());
            System.err.println("   Falling back to Haversine formula");
        }
        
        // Fallback to Haversine formula (straight-line distance)
        System.out.println("📏 Using Haversine formula (straight-line distance)");
        return calculateHaversineDistance(lat1, lon1, lat2, lon2);
    }
    
    /**
     * Calculate straight-line distance using Haversine formula
     * Used as fallback when OSRM is unavailable
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        System.out.println("📏 Calculating distance using Haversine formula");
        System.out.println("   From: (" + lat1 + ", " + lon1 + ")");
        System.out.println("   To: (" + lat2 + ", " + lon2 + ")");
        
        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Calculate differences
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        
        // Haversine formula
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Calculate distance
        double distance = EARTH_RADIUS_KM * c;
        
        // Round to 2 decimal places
        double roundedDistance = Math.round(distance * 100.0) / 100.0;
        System.out.println("✅ Calculated distance: " + roundedDistance + " km");
        
        return roundedDistance;
    }
}

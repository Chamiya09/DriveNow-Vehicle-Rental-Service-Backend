package com.drivenow.controller;

import com.drivenow.entity.UserSettings;
import com.drivenow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferencesController {
    
    private final UserService userService;
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<UserSettings> getUserPreferences(@PathVariable Long userId) {
        try {
            UserSettings settings = userService.getUserSettings(userId);
            return ResponseEntity.ok(settings);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<UserSettings> updateUserPreferences(@PathVariable Long userId, @RequestBody UserSettings settings) {
        try {
            UserSettings updatedSettings = userService.updateUserSettings(userId, settings);
            return ResponseEntity.ok(updatedSettings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{userId}/auto-approve-reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> getAutoApproveReviews(@PathVariable Long userId) {
        try {
            // This is a placeholder - you can add a specific setting for auto-approve
            Map<String, Boolean> response = new HashMap<>();
            response.put("autoApprove", false); // Default value
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{userId}/auto-approve-reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> updateAutoApproveReviews(@PathVariable Long userId, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean autoApprove = request.get("autoApprove");
            // Store this setting - you can add a field to UserSettings or create a separate AdminSettings entity
            Map<String, Boolean> response = new HashMap<>();
            response.put("autoApprove", autoApprove);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

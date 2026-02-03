package com.drivenow.controller;

import com.drivenow.dto.AuthResponse;
import com.drivenow.dto.LoginRequest;
import com.drivenow.dto.RegisterRequest;
import com.drivenow.entity.User;
import com.drivenow.service.AuthService;
import com.drivenow.service.NotificationService;
import com.drivenow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            
            // Send welcome notification to new user
            try {
                User newUser = userRepository.findByEmail(request.getEmail()).orElse(null);
                if (newUser != null) {
                    notificationService.notifyAccountCreated(newUser);
                }
            } catch (Exception e) {
                System.err.println("Failed to send welcome notification: " + e.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage() != null ? e.getMessage() : "Registration failed");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("name", user.getName());
                userInfo.put("email", user.getEmail());
                userInfo.put("role", user.getRole().name());
                userInfo.put("phone", user.getPhone());
                userInfo.put("profileImage", user.getProfileImage());
                userInfo.put("status", user.getStatus().name());
                userInfo.put("licenseNumber", user.getLicenseNumber());
                userInfo.put("available", user.getAvailable());
                userInfo.put("joinDate", user.getCreatedAt());
                
                return ResponseEntity.ok(userInfo);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<Map<String, String>> logout() {
        // With JWT, logout is handled client-side by removing the token
        // This endpoint is provided for consistency and future token blacklisting
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>> checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && 
                                 authentication.isAuthenticated() && 
                                 !"anonymousUser".equals(authentication.getPrincipal());
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("authenticated", isAuthenticated);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DRIVER')")
    public ResponseEntity<AuthResponse> refreshToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                User user = (User) authentication.getPrincipal();
                AuthResponse response = authService.refreshToken(user);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

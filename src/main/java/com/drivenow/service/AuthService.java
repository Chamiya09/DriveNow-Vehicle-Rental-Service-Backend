package com.drivenow.service;

import com.drivenow.dto.AuthResponse;
import com.drivenow.dto.LoginRequest;
import com.drivenow.dto.RegisterRequest;
import com.drivenow.entity.User;
import com.drivenow.entity.UserSettings;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.UserSettingsRepository;
import com.drivenow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAddress(request.getAddress());
        
        // Set role, default to USER
        if (request.getRole() != null && !request.getRole().isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.USER);
            }
        } else {
            user.setRole(User.Role.USER);
        }
        
        // Set driver-specific fields
        if (user.getRole() == User.Role.DRIVER) {
            user.setAvailable(request.getAvailable() != null ? request.getAvailable() : true);
            if (request.getLicenseNumber() != null) {
                user.setLicenseNumber(request.getLicenseNumber());
            }
        }
        
        // Set profile image
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }
        
        // Set driver documents if provided
        if (request.getDriversLicense() != null) {
            user.setDriversLicense(request.getDriversLicense());
        }
        if (request.getVehicleRegistration() != null) {
            user.setVehicleRegistration(request.getVehicleRegistration());
        }
        if (request.getInsuranceCertificate() != null) {
            user.setInsuranceCertificate(request.getInsuranceCertificate());
        }
        
        user.setStatus(User.Status.ACTIVE);
        
        User savedUser = userRepository.save(user);
        
        // Create default user settings
        UserSettings settings = new UserSettings();
        settings.setUser(savedUser);
        userSettingsRepository.save(settings);
        
        String token = jwtUtil.generateToken(savedUser.getEmail());
        
        return AuthResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .token(token)
                .phone(savedUser.getPhone())
                .licenseNumber(savedUser.getLicenseNumber())
                .build();
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .phone(user.getPhone())
                .licenseNumber(user.getLicenseNumber())
                .build();
    }
    
    public AuthResponse refreshToken(User user) {
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .phone(user.getPhone())
                .licenseNumber(user.getLicenseNumber())
                .build();
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

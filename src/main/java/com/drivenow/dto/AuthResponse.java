package com.drivenow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
    private String phone;
    private String licenseNumber;
}

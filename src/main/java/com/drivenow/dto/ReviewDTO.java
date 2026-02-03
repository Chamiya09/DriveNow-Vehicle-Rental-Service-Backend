package com.drivenow.dto;

import com.drivenow.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private Integer rating;
    private String comment;
    private String status;
    private LocalDateTime createdAt;
    
    // User information
    private Long userId;
    private String userName;
    private String userEmail;
    private String userProfileImage;
    
    // Vehicle information
    private Long vehicleId;
    private String vehicleName;
    private String vehicleImage;
    
    public static ReviewDTO fromEntity(Review review) {
        return ReviewDTO.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .status(review.getStatus().name())
                .createdAt(review.getCreatedAt())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : "Anonymous")
                .userEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                .userProfileImage(review.getUser() != null ? review.getUser().getProfileImage() : null)
                .vehicleId(review.getVehicle() != null ? review.getVehicle().getId() : null)
                .vehicleName(review.getVehicle() != null ? review.getVehicle().getName() : "Unknown Vehicle")
                .vehicleImage(review.getVehicle() != null ? review.getVehicle().getImage() : null)
                .build();
    }
}

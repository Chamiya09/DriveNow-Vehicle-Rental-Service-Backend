package com.drivenow.config;

import com.drivenow.entity.Review;
import com.drivenow.entity.User;
import com.drivenow.entity.Vehicle;
import com.drivenow.repository.ReviewRepository;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.VehicleRepository;
import com.drivenow.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Data initializer to populate sample reviews for vehicles
 * DISABLED - Only real user reviews should be shown
 * This will run once when the application starts
 */
// @Component - DISABLED: Don't create sample reviews, only show real user reviews
@RequiredArgsConstructor
@Slf4j
public class ReviewDataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final VehicleService vehicleService;

    @Override
    public void run(String... args) {
        // Only initialize if there are no reviews yet
        if (reviewRepository.count() > 0) {
            log.info("Reviews already exist. Skipping initialization.");
            return;
        }

        log.info("Initializing sample review data...");

        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<User> users = userRepository.findAll();

        if (vehicles.isEmpty()) {
            log.warn("No vehicles found. Skipping review initialization.");
            return;
        }

        if (users.isEmpty()) {
            log.warn("No users found. Skipping review initialization.");
            return;
        }

        // Sample review comments for different ratings
        List<String> excellentComments = Arrays.asList(
            "Excellent vehicle! Very comfortable and well-maintained. The driver was professional and courteous.",
            "Amazing experience! The car was spotless and drove like a dream. Highly recommend!",
            "Perfect for our family trip. Spacious and fuel efficient. Will definitely rent again!",
            "Best rental experience ever! The vehicle exceeded expectations.",
            "Luxury at its finest! Smooth ride and premium features. Worth every penny!",
            "Absolutely loved this car. The comfort level is unmatched!",
            "Professional service and a top-tier vehicle. Couldn't ask for more!",
            "Perfect for business meetings. Arrived in style!",
            "Pure luxury! Every detail is perfect. Premium experience!",
            "Absolutely stunning vehicle. Made our anniversary special!",
            "Reliable and economical. Perfect for daily commuting!",
            "Great fuel efficiency! Saved a lot on our road trip.",
            "Fun to drive! Love the manual transmission.",
            "Sporty and efficient. Enjoyed every moment!",
            "Perfect for moving furniture! Spacious and powerful.",
            "Great for group travel. Fit 10 people comfortably!",
            "Ideal for our team outing. Everyone had plenty of room.",
            "The massage seats are incredible! Top-notch vehicle.",
            "Best car I've ever rented. Exceeded all expectations!"
        );

        List<String> goodComments = Arrays.asList(
            "Great car overall. Minor issue with the AC but everything else was perfect.",
            "Good value for money. Clean and well-maintained.",
            "Decent car for the price. Had a small scratch but overall good.",
            "Good for city driving. A bit tight on space for long trips.",
            "Nice compact car. Perfect for solo travelers.",
            "Meets expectations. Could use better sound system.",
            "Very practical van. Good condition and clean.",
            "Solid van, though fuel consumption is a bit high.",
            "Excellent vehicle, just wish it had more trunk space for luggage."
        );

        List<String> averageComments = Arrays.asList(
            "Average experience. Car was okay but had some wear and tear.",
            "Decent rental. Nothing special but got the job done.",
            "Car was fine for basic needs. Could be cleaner."
        );

        Random random = new Random();

        // Add reviews for each vehicle
        for (Vehicle vehicle : vehicles) {
            int numberOfReviews = 3 + random.nextInt(8); // 3-10 reviews per vehicle
            
            for (int i = 0; i < numberOfReviews; i++) {
                Review review = new Review();
                
                // Random user
                User user = users.get(random.nextInt(users.size()));
                review.setUser(user);
                review.setVehicle(vehicle);
                
                // Generate rating with bias towards higher ratings (more realistic)
                int rating;
                int ratingRoll = random.nextInt(100);
                if (ratingRoll < 60) {
                    rating = 5; // 60% chance of 5 stars
                } else if (ratingRoll < 85) {
                    rating = 4; // 25% chance of 4 stars
                } else if (ratingRoll < 95) {
                    rating = 3; // 10% chance of 3 stars
                } else {
                    rating = 2; // 5% chance of 2 stars
                }
                
                review.setRating(rating);
                
                // Select appropriate comment based on rating
                String comment;
                if (rating >= 5) {
                    comment = excellentComments.get(random.nextInt(excellentComments.size()));
                } else if (rating >= 4) {
                    comment = goodComments.get(random.nextInt(goodComments.size()));
                } else {
                    comment = averageComments.get(random.nextInt(averageComments.size()));
                }
                
                review.setComment(comment);
                review.setStatus(Review.ReviewStatus.APPROVED);
                
                reviewRepository.save(review);
            }
            
            // Update vehicle rating after adding reviews
            vehicleService.updateVehicleRating(vehicle.getId());
            
            log.info("Added {} reviews for vehicle: {} (Rating: {})", 
                numberOfReviews, vehicle.getName(), vehicle.getRating());
        }

        log.info("Sample review data initialization completed!");
        log.info("Total reviews created: {}", reviewRepository.count());
    }
}

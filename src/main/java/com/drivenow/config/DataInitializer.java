package com.drivenow.config;

import com.drivenow.entity.User;
import com.drivenow.entity.UserSettings;
import com.drivenow.entity.Vehicle;
import com.drivenow.repository.UserRepository;
import com.drivenow.repository.UserSettingsRepository;
import com.drivenow.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final PasswordEncoder passwordEncoder;
    
    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, 
                                   VehicleRepository vehicleRepository,
                                   UserSettingsRepository userSettingsRepository) {
        return args -> {
            // Create admin user
            if (!userRepository.existsByEmail("admin@drivenow.com")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@drivenow.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setPhone("+1234567890");
                admin.setRole(User.Role.ADMIN);
                admin.setStatus(User.Status.ACTIVE);
                User savedAdmin = userRepository.save(admin);
                
                // Create settings for admin
                UserSettings adminSettings = new UserSettings();
                adminSettings.setUser(savedAdmin);
                userSettingsRepository.save(adminSettings);
                
                log.info("Admin user created: admin@drivenow.com / admin123");
            } else {
                log.info("Admin user already exists");
            }
            
            // Create sample driver
            if (!userRepository.existsByEmail("driver@drivenow.com")) {
                User driver = new User();
                driver.setName("John Driver");
                driver.setEmail("driver@drivenow.com");
                driver.setPassword(passwordEncoder.encode("driver123"));
                driver.setPhone("+1234567891");
                driver.setRole(User.Role.DRIVER);
                driver.setStatus(User.Status.ACTIVE);
                driver.setLicenseNumber("DL123456");
                driver.setAvailable(true);
                User savedDriver = userRepository.save(driver);
                
                // Create settings for driver
                UserSettings driverSettings = new UserSettings();
                driverSettings.setUser(savedDriver);
                userSettingsRepository.save(driverSettings);
                
                log.info("Driver user created: driver@drivenow.com / driver123");
            }
            
            // Create sample regular user
            if (!userRepository.existsByEmail("user@drivenow.com")) {
                User user = new User();
                user.setName("Test User");
                user.setEmail("user@drivenow.com");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setPhone("+1234567892");
                user.setRole(User.Role.USER);
                user.setStatus(User.Status.ACTIVE);
                User savedUser = userRepository.save(user);
                
                // Create settings for user
                UserSettings userSettings = new UserSettings();
                userSettings.setUser(savedUser);
                userSettingsRepository.save(userSettings);
                
                log.info("Regular user created: user@drivenow.com / user123");
            }
            
            // Create sample vehicles
            if (vehicleRepository.count() == 0) {
                createSampleVehicles(vehicleRepository);
                log.info("Sample vehicles created successfully");
            } else {
                log.info("Sample vehicles already exist");
            }
        };
    }
    
    private void createSampleVehicles(VehicleRepository vehicleRepository) {
        // Vehicle 1: Toyota Camry
        Vehicle camry = new Vehicle();
        camry.setName("Toyota Camry 2024");
        camry.setCategory(Vehicle.VehicleCategory.SEDAN);
        camry.setPricePerDay(new BigDecimal("75.00"));
        camry.setImage("https://images.unsplash.com/photo-1621007947382-bb3c3994e3fb?w=800");
        camry.setRating(4.8);
        camry.setReviewCount(124);
        camry.setSeats(5);
        camry.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        camry.setFuelType(Vehicle.FuelType.HYBRID);
        camry.setFeatures(Set.of("Bluetooth", "Backup Camera", "Lane Assist", "Apple CarPlay", "Cruise Control"));
        camry.setAvailable(true);
        camry.setDescription("The Toyota Camry offers a perfect blend of comfort, reliability, and efficiency. Ideal for business trips or family vacations.");
        camry.setLicensePlate("CAM-2024");
        camry.setYear(2024);
        camry.setColor("Silver");
        vehicleRepository.save(camry);
        
        // Vehicle 2: Honda CR-V
        Vehicle crv = new Vehicle();
        crv.setName("Honda CR-V 2024");
        crv.setCategory(Vehicle.VehicleCategory.SUV);
        crv.setPricePerDay(new BigDecimal("95.00"));
        crv.setImage("https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800");
        crv.setRating(4.7);
        crv.setReviewCount(98);
        crv.setSeats(7);
        crv.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        crv.setFuelType(Vehicle.FuelType.PETROL);
        crv.setFeatures(Set.of("4WD", "Sunroof", "Leather Seats", "Navigation", "Parking Sensors"));
        crv.setAvailable(true);
        crv.setDescription("Spacious SUV perfect for family adventures. Features all-wheel drive and plenty of cargo space.");
        crv.setLicensePlate("CRV-2024");
        crv.setYear(2024);
        crv.setColor("Black");
        vehicleRepository.save(crv);
        
        // Vehicle 3: Tesla Model 3
        Vehicle tesla = new Vehicle();
        tesla.setName("Tesla Model 3");
        tesla.setCategory(Vehicle.VehicleCategory.LUXURY);
        tesla.setPricePerDay(new BigDecimal("150.00"));
        tesla.setImage("https://images.unsplash.com/photo-1560958089-b8a1929cea89?w=800");
        tesla.setRating(4.9);
        tesla.setReviewCount(156);
        tesla.setSeats(5);
        tesla.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        tesla.setFuelType(Vehicle.FuelType.ELECTRIC);
        tesla.setFeatures(Set.of("Autopilot", "Premium Audio", "Glass Roof", "Heated Seats", "Supercharging"));
        tesla.setAvailable(true);
        tesla.setDescription("Experience the future of driving with Tesla's electric sedan. Advanced autopilot and zero emissions.");
        tesla.setLicensePlate("TSLA-2024");
        tesla.setYear(2024);
        tesla.setColor("White");
        vehicleRepository.save(tesla);
        
        // Vehicle 4: Ford Mustang
        Vehicle mustang = new Vehicle();
        mustang.setName("Ford Mustang GT");
        mustang.setCategory(Vehicle.VehicleCategory.LUXURY);
        mustang.setPricePerDay(new BigDecimal("180.00"));
        mustang.setImage("https://images.unsplash.com/photo-1584345604476-8ec5f8f2ca7c?w=800");
        mustang.setRating(4.9);
        mustang.setReviewCount(87);
        mustang.setSeats(4);
        mustang.setTransmission(Vehicle.TransmissionType.MANUAL);
        mustang.setFuelType(Vehicle.FuelType.PETROL);
        mustang.setFeatures(Set.of("Sport Mode", "Premium Sound", "Performance Package", "Track Apps"));
        mustang.setAvailable(true);
        mustang.setDescription("Iconic American muscle car with powerful performance and head-turning style.");
        mustang.setLicensePlate("MUST-2024");
        mustang.setYear(2024);
        mustang.setColor("Red");
        vehicleRepository.save(mustang);
        
        // Vehicle 5: Toyota Corolla
        Vehicle corolla = new Vehicle();
        corolla.setName("Toyota Corolla 2024");
        corolla.setCategory(Vehicle.VehicleCategory.SEDAN);
        corolla.setPricePerDay(new BigDecimal("55.00"));
        corolla.setImage("https://images.unsplash.com/photo-1623869675781-80aa31baa942?w=800");
        corolla.setRating(4.6);
        corolla.setReviewCount(203);
        corolla.setSeats(5);
        corolla.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        corolla.setFuelType(Vehicle.FuelType.PETROL);
        corolla.setFeatures(Set.of("Bluetooth", "Backup Camera", "Fuel Efficient", "USB Ports"));
        corolla.setAvailable(true);
        corolla.setDescription("Reliable and economical sedan perfect for daily commutes and city driving.");
        corolla.setLicensePlate("COR-2024");
        corolla.setYear(2024);
        corolla.setColor("Blue");
        vehicleRepository.save(corolla);
        
        // Vehicle 6: Mercedes-Benz E-Class
        Vehicle mercedes = new Vehicle();
        mercedes.setName("Mercedes-Benz E-Class");
        mercedes.setCategory(Vehicle.VehicleCategory.LUXURY);
        mercedes.setPricePerDay(new BigDecimal("200.00"));
        mercedes.setImage("https://images.unsplash.com/photo-1618843479313-40f8afb4b4d8?w=800");
        mercedes.setRating(5.0);
        mercedes.setReviewCount(64);
        mercedes.setSeats(5);
        mercedes.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        mercedes.setFuelType(Vehicle.FuelType.DIESEL);
        mercedes.setFeatures(Set.of("Luxury Interior", "Massage Seats", "Burmester Sound", "Ambient Lighting", "Panoramic Roof"));
        mercedes.setAvailable(true);
        mercedes.setDescription("Ultimate luxury sedan with cutting-edge technology and unmatched comfort.");
        mercedes.setLicensePlate("BENZ-2024");
        mercedes.setYear(2024);
        mercedes.setColor("Black");
        vehicleRepository.save(mercedes);
        
        // Vehicle 7: Honda Civic
        Vehicle civic = new Vehicle();
        civic.setName("Honda Civic 2024");
        civic.setCategory(Vehicle.VehicleCategory.HATCHBACK);
        civic.setPricePerDay(new BigDecimal("65.00"));
        civic.setImage("https://images.unsplash.com/photo-1590362891991-f776e747a588?w=800");
        civic.setRating(4.7);
        civic.setReviewCount(142);
        civic.setSeats(5);
        civic.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        civic.setFuelType(Vehicle.FuelType.PETROL);
        civic.setFeatures(Set.of("Sporty Design", "Touchscreen", "Keyless Entry", "Android Auto"));
        civic.setAvailable(true);
        civic.setDescription("Sporty and fun-to-drive hatchback with modern technology and great fuel economy.");
        civic.setLicensePlate("CIV-2024");
        civic.setYear(2024);
        civic.setColor("Gray");
        vehicleRepository.save(civic);
        
        // Vehicle 8: Ford Explorer
        Vehicle explorer = new Vehicle();
        explorer.setName("Ford Explorer 2024");
        explorer.setCategory(Vehicle.VehicleCategory.SUV);
        explorer.setPricePerDay(new BigDecimal("110.00"));
        explorer.setImage("https://images.unsplash.com/photo-1519641471654-76ce0107ad1b?w=800");
        explorer.setRating(4.8);
        explorer.setReviewCount(76);
        explorer.setSeats(7);
        explorer.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        explorer.setFuelType(Vehicle.FuelType.PETROL);
        explorer.setFeatures(Set.of("Third Row Seating", "Towing Package", "SYNC 3", "Blind Spot Monitor"));
        explorer.setAvailable(true);
        explorer.setDescription("Full-size SUV with three rows of seating. Perfect for large families and road trips.");
        explorer.setLicensePlate("EXP-2024");
        explorer.setYear(2024);
        explorer.setColor("Silver");
        vehicleRepository.save(explorer);
        
        // Vehicle 9: Toyota Sienna
        Vehicle sienna = new Vehicle();
        sienna.setName("Toyota Sienna 2024");
        sienna.setCategory(Vehicle.VehicleCategory.VAN);
        sienna.setPricePerDay(new BigDecimal("120.00"));
        sienna.setImage("https://images.unsplash.com/photo-1527786356703-4b100091cd2c?w=800");
        sienna.setRating(4.7);
        sienna.setReviewCount(91);
        sienna.setSeats(8);
        sienna.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        sienna.setFuelType(Vehicle.FuelType.HYBRID);
        sienna.setFeatures(Set.of("Sliding Doors", "Entertainment System", "Captain Chairs", "AWD Available"));
        sienna.setAvailable(true);
        sienna.setDescription("Spacious family minivan with hybrid efficiency. Ideal for group travel and family trips.");
        sienna.setLicensePlate("SIE-2024");
        sienna.setYear(2024);
        sienna.setColor("White");
        vehicleRepository.save(sienna);
        
        // Vehicle 10: Mazda CX-5
        Vehicle mazda = new Vehicle();
        mazda.setName("Mazda CX-5 2024");
        mazda.setCategory(Vehicle.VehicleCategory.SUV);
        mazda.setPricePerDay(new BigDecimal("85.00"));
        mazda.setImage("https://images.unsplash.com/photo-1607077520734-b971b6289e3c?w=800");
        mazda.setRating(4.8);
        mazda.setReviewCount(118);
        mazda.setSeats(5);
        mazda.setTransmission(Vehicle.TransmissionType.AUTOMATIC);
        mazda.setFuelType(Vehicle.FuelType.PETROL);
        mazda.setFeatures(Set.of("i-ACTIVSENSE", "Bose Audio", "Head-Up Display", "Smart City Brake"));
        mazda.setAvailable(true);
        mazda.setDescription("Stylish compact SUV with upscale interior and engaging driving dynamics.");
        mazda.setLicensePlate("MAZ-2024");
        mazda.setYear(2024);
        mazda.setColor("Red");
        vehicleRepository.save(mazda);
    }
}

package com.drivenow.repository;

import com.drivenow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(User.Role role);
    List<User> findByStatus(User.Status status);
    
    @Query("SELECT u FROM User u WHERE u.role = 'DRIVER' AND u.available = true")
    List<User> findAvailableDrivers();
    
    @Query("SELECT u FROM User u WHERE u.role = 'DRIVER' AND u.status = 'ACTIVE'")
    List<User> findAllActiveDrivers();
}

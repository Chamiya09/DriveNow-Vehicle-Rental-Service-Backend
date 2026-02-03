package com.drivenow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DriveNowApplication {
    public static void main(String[] args) {
        SpringApplication.run(DriveNowApplication.class, args);
    }
}

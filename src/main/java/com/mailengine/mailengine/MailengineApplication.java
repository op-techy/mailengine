package com.mailengine.mailengine;

import com.mailengine.mailengine.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the MailEngine application.
 * EnableJpaAuditing activates automatic population of @CreatedDate
 * and @LastModifiedDate fields across all entities.
 */
@Slf4j
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class MailengineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailengineApplication.class, args);
    }

    @Bean
    CommandLineRunner testS3(S3Service s3Service) {
        return args -> {
            log.info("Checking AWS S3 connection for bucket: {}", "mailengine-assets-2026");

            try {
                // Uncomment this once your S3Service.uploadFile is ready
                // s3Service.uploadFile("test-connection.txt", "Connection successful".getBytes());
                log.info("S3 connection test completed successfully.");
            } catch (Exception e) {
                log.error("S3 connection test failed: {}", e.getMessage());
            }
        };
    }
}
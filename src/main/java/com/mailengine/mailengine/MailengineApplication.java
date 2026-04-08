package com.mailengine.mailengine;

import com.mailengine.mailengine.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import com.mailengine.mailengine.service.EmailService;

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
}
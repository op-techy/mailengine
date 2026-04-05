package com.mailengine.mailengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Entry point for the MailEngine application.
 * EnableJpaAuditing activates automatic population of @CreatedDate
 * and @LastModifiedDate fields across all entities.
 */
@SpringBootApplication
@EnableJpaAuditing
public class MailengineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailengineApplication.class, args);
    }
}
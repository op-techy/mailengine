package com.mailengine.mailengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the MailEngine application.
 * EnableJpaAuditing activates automatic population of @CreatedDate
 * and @LastModifiedDate fields across all entities.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class MailengineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailengineApplication.class, args);
    }
}
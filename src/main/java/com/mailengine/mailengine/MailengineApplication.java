package com.mailengine.mailengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MailengineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MailengineApplication.class, args);
    }

}

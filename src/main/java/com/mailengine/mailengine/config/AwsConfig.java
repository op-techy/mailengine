package com.mailengine.mailengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * Uses DefaultCredentialsProvider — reads credentials from:
     * 1. Environment variables AWS_ACCESS_KEY_ID / AWS_SECRET_ACCESS_KEY
     * 2. ~/.aws/credentials (local dev)
     * 3. EC2/ECS IAM role (production)
     * Never hard-code credentials in source code.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}

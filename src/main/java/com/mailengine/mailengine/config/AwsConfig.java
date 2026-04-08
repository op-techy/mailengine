package com.mailengine.mailengine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    private DefaultCredentialsProvider credentialsProvider() {
        return DefaultCredentialsProvider.create();
    }

    /**
     * Creates and configures an instance of the S3Client for interacting with AWS S3.
     * The client is set up with a specified AWS region and a default credentials provider.
     *
     * @return an instance of S3Client configured with the specified region and default credentials provider.
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    /**
     * Creates and configures an instance of the SesClient for interacting with AWS Simple Email Service (SES).
     * The client is setup with a specified AWS region and credentials provider.
     *
     * @return an instance of SesClient configured with the specified region and credentials provider.
     */
    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider())
                .build();
    }
}

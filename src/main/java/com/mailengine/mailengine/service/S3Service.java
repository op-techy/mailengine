package com.mailengine.mailengine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.cdn-base-url}")
    private String cdnBaseUrl;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads an image file to S3 under the "images/" prefix.
     *
     * @return the public CDN URL of the uploaded image
     */
    public String uploadImage(MultipartFile file) {
        validateContentType(file, "image/");
        String key = "images/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        return upload(file, key);
    }

    /**
     * Uploads an HTML file to S3 under the "uploads/html/" prefix.
     *
     * @return the public CDN URL
     */
    public String uploadHtml(MultipartFile file) {
        String key = "uploads/html/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        return upload(file, key);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private String upload(MultipartFile file, String key) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
            String url = cdnBaseUrl + "/" + key;
            log.info("Uploaded file to S3: {}", url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    private void validateContentType(MultipartFile file, String expectedPrefix) {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                    "Invalid file type: " + ct + ". Expected " + expectedPrefix + "*");
        }
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    }
}

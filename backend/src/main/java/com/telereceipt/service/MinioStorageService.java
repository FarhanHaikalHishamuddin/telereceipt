package com.telereceipt.service;

import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioStorageService {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;
    private final String bucketName;
    private MinioClient minioClient;

    public MinioStorageService(
            @Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.access-key:minioadmin}") String accessKey,
            @Value("${minio.secret-key:minioadminpassword}") String secretKey,
            @Value("${minio.bucket-name:receipts}") String bucketName) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void init() {
        try {
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize MinIO bucket (is MinIO running?): " + e.getMessage());
        }
    }

    public String uploadImage(String objectName, byte[] data, String contentType) {
        try {
            if (minioClient == null) {
                init();
            }
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
                            .contentType(contentType != null ? contentType : "image/jpeg")
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            System.err.println("Failed to upload image to MinIO: " + e.getMessage());
            return null;
        }
    }

    public String getPresignedUrl(String objectName) {
        if (objectName == null || objectName.isEmpty() || minioClient == null) {
            return null;
        }
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(15, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            System.err.println("Failed to get presigned URL: " + e.getMessage());
            return null;
        }
    }
}

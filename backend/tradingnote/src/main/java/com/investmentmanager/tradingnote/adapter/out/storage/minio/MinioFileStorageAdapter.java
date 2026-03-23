package com.investmentmanager.tradingnote.adapter.out.storage.minio;

import com.investmentmanager.tradingnote.domain.port.out.FileStoragePort;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@Profile("minio")
public class MinioFileStorageAdapter implements FileStoragePort {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorageAdapter.class);

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioFileStorageAdapter(
            @Value("${minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${minio.access-key:minioadmin}") String accessKey,
            @Value("${minio.secret-key:minioadmin}") String secretKey,
            @Value("${minio.bucket:trading-notes}") String bucketName) {
        this.bucketName = bucketName;
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Created MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.warn("Could not ensure bucket exists: {}", e.getMessage());
        }
    }

    @Override
    public String store(String filename, InputStream content, long size) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(content, size, -1)
                    .contentType("application/pdf")
                    .build());
            String reference = bucketName + "/" + filename;
            log.info("Stored file in MinIO: {}", reference);
            return reference;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file in MinIO: " + filename, e);
        }
    }

    @Override
    public InputStream retrieve(String fileReference) {
        try {
            String object = fileReference.replace(bucketName + "/", "");
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(object)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve file from MinIO: " + fileReference, e);
        }
    }

    @Override
    public void delete(String fileReference) {
        try {
            String object = fileReference.replace(bucketName + "/", "");
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(object)
                    .build());
            log.info("Deleted file from MinIO: {}", fileReference);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO: " + fileReference, e);
        }
    }
}

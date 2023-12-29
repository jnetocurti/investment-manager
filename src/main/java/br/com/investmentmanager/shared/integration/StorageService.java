package br.com.investmentmanager.shared.integration;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class StorageService {

    private final MinioClient client;

    public void storage(@NonNull String name, @NonNull String contentType, @NonNull InputStream object, @NonNull String bucket) {
        try {

            if (!client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }

            var args = PutObjectArgs.builder().object(name).contentType(contentType)
                    .stream(object, object.available(), -1).bucket(bucket).build();

            client.putObject(args);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

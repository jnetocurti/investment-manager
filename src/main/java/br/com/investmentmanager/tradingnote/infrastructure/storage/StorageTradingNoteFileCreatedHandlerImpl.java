package br.com.investmentmanager.tradingnote.infrastructure.storage;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.events.handler.StorageTradingNoteFileCreatedHandler;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StorageTradingNoteFileCreatedHandlerImpl implements StorageTradingNoteFileCreatedHandler {

    public static final String TRADING_NOTES_FOLDER = "trading_notes/";

    private final MinioClient minioClient;

    @Value("${spring.minio.bucket}")
    private String bucket;

    @Override
    public void handle(@NonNull TradingNoteFileCreated event) {
        try {
            createBucketIfNotExists();

            var file = event.getSource();
            var content = file.getContent();

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(TRADING_NOTES_FOLDER + file.getName())
                    .contentType(file.getContentType())
                    .stream(content, content.available(), -1)
                    .build());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createBucketIfNotExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}

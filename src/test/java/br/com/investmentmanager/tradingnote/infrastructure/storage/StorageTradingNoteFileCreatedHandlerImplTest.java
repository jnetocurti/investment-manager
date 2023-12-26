package br.com.investmentmanager.tradingnote.infrastructure.storage;

import br.com.investmentmanager.tradingnote.domain.events.TradingNoteFileCreated;
import br.com.investmentmanager.tradingnote.domain.valueobjects.TradingNoteFile;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageTradingNoteFileCreatedHandlerImplTest {

    private final String bucket = "my-bucket";

    @InjectMocks
    private StorageTradingNoteFileCreatedHandlerImpl handler;
    @Mock
    private MinioClient minioClient;
    @Captor
    private ArgumentCaptor<PutObjectArgs> putObjectArgsArgumentCaptor;
    @Captor
    private ArgumentCaptor<BucketExistsArgs> bucketExistsArgsArgumentCaptor;
    @Captor
    private ArgumentCaptor<MakeBucketArgs> makeBucketArgsArgumentCaptor;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(handler, "bucket", bucket);
    }

    @SneakyThrows
    @Test
    void handle() {
        when(minioClient.bucketExists(any())).thenReturn(true);

        var content = ByteArrayInputStream.nullInputStream();
        var generator = new EasyRandom(new EasyRandomParameters().randomize(InputStream.class, () -> content));
        var tradingNoteFileCreated = new TradingNoteFileCreated(generator.nextObject(TradingNoteFile.class));


        handler.handle(tradingNoteFileCreated);

        verify(minioClient, times(1)).putObject(putObjectArgsArgumentCaptor.capture());
        verify(minioClient, times(1)).bucketExists(bucketExistsArgsArgumentCaptor.capture());
        verify(minioClient, never()).makeBucket(any());

        var tradingNoteFile = tradingNoteFileCreated.getSource();
        assertThat(putObjectArgsArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("stream")
                .isEqualTo(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object("trading_notes/" + tradingNoteFile.getName())
                        .stream(content, content.available(), -1)
                        .contentType(tradingNoteFile.getContentType())
                        .build());

        assertThat(bucketExistsArgsArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("stream")
                .isEqualTo(BucketExistsArgs.builder().bucket(bucket).build());
    }

    @SneakyThrows
    @Test
    void handleCreateBucket() {
        when(minioClient.bucketExists(any())).thenReturn(false);

        var content = ByteArrayInputStream.nullInputStream();
        var generator = new EasyRandom(new EasyRandomParameters().randomize(InputStream.class, () -> content));
        var tradingNoteFileCreated = new TradingNoteFileCreated(generator.nextObject(TradingNoteFile.class));

        handler.handle(tradingNoteFileCreated);

        verify(minioClient, times(1)).putObject(putObjectArgsArgumentCaptor.capture());
        verify(minioClient, times(1)).bucketExists(bucketExistsArgsArgumentCaptor.capture());
        verify(minioClient, times(1)).makeBucket(makeBucketArgsArgumentCaptor.capture());

        var tradingNoteFile = tradingNoteFileCreated.getSource();
        assertThat(putObjectArgsArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("stream")
                .isEqualTo(PutObjectArgs.builder()
                        .bucket(bucket)
                        .object("trading_notes/" + tradingNoteFile.getName())
                        .stream(content, content.available(), -1)
                        .contentType(tradingNoteFile.getContentType())
                        .build());

        assertThat(bucketExistsArgsArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("stream")
                .isEqualTo(BucketExistsArgs.builder().bucket(bucket).build());

        assertThat(makeBucketArgsArgumentCaptor.getValue()).usingRecursiveComparison()
                .ignoringFields("stream")
                .isEqualTo(MakeBucketArgs.builder().bucket(bucket).build());
    }

    @SneakyThrows
    @Test
    void handleThrowsRuntimeException() {
        when(minioClient.bucketExists(any())).thenThrow(new IOException());

        var generator = new EasyRandom(new EasyRandomParameters().randomize(InputStream.class, ByteArrayInputStream::nullInputStream));
        var tradingNoteFileCreated = new TradingNoteFileCreated(generator.nextObject(TradingNoteFile.class));

        assertThrows(RuntimeException.class, () -> handler.handle(tradingNoteFileCreated));
    }
}
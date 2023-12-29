package br.com.investmentmanager.shared.integration;

import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.SneakyThrows;
import org.jeasy.random.EasyRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @InjectMocks
    private StorageService service;
    @Mock
    private MinioClient client;
    @Captor
    private ArgumentCaptor<PutObjectArgs> captor;

    @Test
    @SneakyThrows
    void storageShouldCreateTheBucketAndStorageTheFile() {
        var easyRandom = new EasyRandom();
        var name = easyRandom.nextObject(String.class);
        var contentType = easyRandom.nextObject(String.class);

        var bucket = "my-bucket";
        var content = new ByteArrayInputStream(new byte[]{});

        when(client.bucketExists(any())).thenReturn(false);

        service.storage(name, contentType, content, bucket);

        verify(client, times(1)).makeBucket(MakeBucketArgs.builder().bucket(bucket).build());

        verify(client, times(1)).putObject(captor.capture());
        assertEquals(name, captor.getValue().object());
        assertEquals(contentType, captor.getValue().contentType());
        assertNotNull(captor.getValue().stream().readAllBytes());
    }

    @Test
    @SneakyThrows
    void storageShouldOnlyStorageTheFile() {
        var easyRandom = new EasyRandom();
        var name = easyRandom.nextObject(String.class);
        var contentType = easyRandom.nextObject(String.class);

        var bucket = "my-bucket";
        var content = new ByteArrayInputStream(new byte[]{});

        when(client.bucketExists(any())).thenReturn(true);

        service.storage(name, contentType, content, bucket);

        verify(client, never()).makeBucket(any(MakeBucketArgs.class));

        verify(client, times(1)).putObject(captor.capture());
        assertEquals(name, captor.getValue().object());
        assertEquals(contentType, captor.getValue().contentType());
        assertNotNull(captor.getValue().stream().readAllBytes());
    }
}
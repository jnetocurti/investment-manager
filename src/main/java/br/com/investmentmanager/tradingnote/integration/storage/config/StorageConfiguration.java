package br.com.investmentmanager.tradingnote.integration.storage.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Value("${storage.url}")
    private String url;
    @Value("${storage.credentials.accessKey}")
    private String accessKey;
    @Value("${storage.credentials.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() throws Exception {
        return MinioClient.builder().endpoint(url).credentials(accessKey, secretKey).build();
    }
}

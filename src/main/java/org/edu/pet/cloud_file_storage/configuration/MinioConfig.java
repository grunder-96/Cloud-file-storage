package org.edu.pet.cloud_file_storage.configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${MINIO_ACCESS_KEY:minio-user}")
    private String accessKey;
    @Value("${MINIO_SECRET_KEY:miniousrpass}")
    private String secretKey;
    @Value("${MINIO_PORT:9000}")
    private int port;

    @Bean
    public MinioClient minioClient() {

        return MinioClient.builder()
                .endpoint("http://localhost:%d/".formatted(port))
                .credentials(accessKey, secretKey)
                .build();
    }
}
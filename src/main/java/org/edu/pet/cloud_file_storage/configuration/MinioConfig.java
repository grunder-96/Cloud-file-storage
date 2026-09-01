package org.edu.pet.cloud_file_storage.configuration;

import io.minio.MinioClient;
import org.edu.pet.cloud_file_storage.configuration.properties.MinioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({MinioProperties.class})
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {

        return MinioClient.builder()
                .endpoint(minioProperties.url())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }
}
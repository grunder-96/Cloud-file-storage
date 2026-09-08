package org.edu.pet.cloud_file_storage.config;

import io.minio.MinioClient;
import org.mockito.BDDMockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MinioTestConfig {

    @Bean
    @Primary
    public MinioClient testMinioClient() {
        return BDDMockito.mock(MinioClient.class);
    }
}
package org.edu.pet.cloud_file_storage.configuration.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(String bucket,
                              String url,
                              String accessKey,
                              String secretKey,
                              String userFolderTemplate) {

}
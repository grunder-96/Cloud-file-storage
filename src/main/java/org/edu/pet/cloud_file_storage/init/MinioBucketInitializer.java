package org.edu.pet.cloud_file_storage.init;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MinioBucketInitializer {

    private final S3Storage s3Storage;

    private final String bucket;

    public MinioBucketInitializer(S3Storage s3Storage,
                                  @Value("${minio.bucket}") String bucket) {

        this.s3Storage = s3Storage;
        this.bucket = bucket;
    }

    @PostConstruct
    public void init() {

        if (s3Storage.bucketExists(bucket)) {

            log.info("Default bucket {} already exists", bucket);
            return;
        }

        s3Storage.createBucket(bucket);
        log.info("Default bucket {} successfully created", bucket);
    }
}
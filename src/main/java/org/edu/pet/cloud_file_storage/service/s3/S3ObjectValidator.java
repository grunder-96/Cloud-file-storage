package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3ObjectValidator {

    private final S3Storage storage;

    @Value("${minio.bucket}")
    private String bucket;

    public void validateNameCollision(S3Key key) {

        S3Key opposite = key.opposite();

        if (storage.objectExists(bucket, opposite)) {

            log.warn("name collision: {} key conflicts with already existing {} key", key.value(), opposite.value());
            throw new InvalidPathException("There cannot be a folder and a file with the same name in one directory");
        }
    }
}
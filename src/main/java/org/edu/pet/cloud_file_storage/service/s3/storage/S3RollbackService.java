package org.edu.pet.cloud_file_storage.service.s3.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.exception.S3PartialRemoveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class S3RollbackService {

    private static final int LOGGED_KEYS_LIMIT = 20;

    private final S3Storage storage;

    @Value("${minio.bucket}")
    private String bucket;

    public void rollback(List<S3Key> keys, String operation) {

        if (Objects.isNull(keys) || keys.isEmpty()) {
            return;
        }

        try {

            storage.remove(bucket, keys);
            log.info("Rollback of {} successful: removed {} keys", operation, keys.size());
        } catch (S3PartialRemoveException e) {

            log.error("Rollback of {} incomplete: {} of {} keys remain in storage: {}",
                    operation, e.getFailedKeys().size(), keys.size(), preview(e.getFailedKeys()));
        } catch (Exception e) {

            log.error("Rollback of {} failed: up to {} keys may remain in storage: {}",
                    operation, keys.size(),
                    preview(keys.stream().map(S3Key::value).toList()), e);
        }
    }

    private List<String> preview(List<String> keys) {
        return keys.size() <= LOGGED_KEYS_LIMIT ? keys : keys.subList(0, LOGGED_KEYS_LIMIT);
    }
}

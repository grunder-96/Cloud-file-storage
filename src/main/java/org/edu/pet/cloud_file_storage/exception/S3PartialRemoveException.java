package org.edu.pet.cloud_file_storage.exception;

import java.util.List;

public class S3PartialRemoveException extends S3ExecutionException {

    private final List<String> failedKeys;

    public S3PartialRemoveException(List<String> failedKeys) {
        super("Failed to remove %s objects".formatted(failedKeys.size()));
        this.failedKeys = failedKeys;
    }

    public List<String> getFailedKeys() {
        return failedKeys;
    }
}
package org.edu.pet.cloud_file_storage.exception;

public class S3ExecutionException extends RuntimeException {

    public S3ExecutionException(String message) {
        super(message);
    }

    public S3ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
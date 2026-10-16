package org.edu.pet.cloud_file_storage.exception;

public class S3ResourceNotFoundException extends RuntimeException {

    public S3ResourceNotFoundException(String message) {
        super(message);
    }

    public S3ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

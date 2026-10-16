package org.edu.pet.cloud_file_storage.exception;

public class S3ResourceAlreadyExistsException extends RuntimeException {

    public S3ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public S3ResourceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
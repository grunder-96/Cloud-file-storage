package org.edu.pet.cloud_file_storage.exception;

public class S3PreconditionFailedException extends RuntimeException {

    public S3PreconditionFailedException(String message) {
        super(message);
    }

    public S3PreconditionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

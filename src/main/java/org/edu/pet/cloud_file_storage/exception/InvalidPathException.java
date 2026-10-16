package org.edu.pet.cloud_file_storage.exception;

public class InvalidPathException extends RuntimeException {

    public InvalidPathException(String message) {
        super(message);
    }

    public InvalidPathException(String message, Throwable cause) {
    }
}
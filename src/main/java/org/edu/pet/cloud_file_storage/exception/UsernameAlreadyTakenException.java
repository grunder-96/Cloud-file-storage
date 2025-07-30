package org.edu.pet.cloud_file_storage.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UsernameAlreadyTakenException extends RuntimeException {

    public UsernameAlreadyTakenException(String message) {
        super(message);
    }

    public UsernameAlreadyTakenException(String message, Throwable cause) {
        super(message, cause);
    }
}
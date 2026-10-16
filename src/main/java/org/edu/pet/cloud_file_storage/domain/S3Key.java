package org.edu.pet.cloud_file_storage.domain;

import org.edu.pet.cloud_file_storage.exception.InvalidPathException;

import java.nio.charset.StandardCharsets;

public final class S3Key {

    private static final int MAX_KEY_BYTES = 1024;

    private static final String PATH_DELIMITER = "/";

    private final String value;

    private final boolean isDirectory;

    public S3Key(String value) {

        if (value == null || value.isBlank()) {
            throw new InvalidPathException("S3 key must not be blank");
        }

        if (value.getBytes(StandardCharsets.UTF_8).length > MAX_KEY_BYTES) {
            throw new InvalidPathException("Path is too long. Please use a shorter value.");
        }

        this.value = value;
        this.isDirectory = value.endsWith(PATH_DELIMITER);
    }

    public String value() {
        return value;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public S3Key opposite() {

        return isDirectory ? new S3Key(withoutTrailingSlash())
            : new S3Key(value + PATH_DELIMITER);
    }

    public S3Key parent() {

        String trimmed = withoutTrailingSlash();
        int lastDelimiter = trimmed.lastIndexOf(PATH_DELIMITER);

        if (lastDelimiter == -1) {
            throw new IllegalStateException("Key has no parent directory: " + value);
        }

        return new S3Key(trimmed.substring(0, lastDelimiter + 1));
    }

    private String withoutTrailingSlash() {
        return isDirectory ? value.substring(0, value.length() - 1) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        S3Key s3Key = (S3Key) o;
        return value.equals(s3Key.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
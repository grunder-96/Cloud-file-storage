package org.edu.pet.cloud_file_storage.domain;

import lombok.ToString;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;

@ToString(exclude = {"isDirectory"})
public abstract class BasePath {

    protected static final String EMPTY = "";

    public static final String PATH_DELIMITER = "/";

    protected final String value;

    private final boolean isDirectory;

    protected BasePath(String value) {

        if (value == null) {
            throw new InvalidPathException("The path cannot be null");
        }

        this.value = value;

        isDirectory = computeIsDirectory();
    }

    public String value() {
        return value;
    }

    public String name() {

        if (isRoot()) {
            return EMPTY;
        }

        String trimmed = withoutTrailingSlash();

        int lastDelimiter = trimmed.lastIndexOf(PATH_DELIMITER);

        String name = lastDelimiter == -1 ? trimmed : trimmed.substring(lastDelimiter + 1);

        return isDirectory() ? name + PATH_DELIMITER : name;
    }

    public String parent() {

        if (isRoot()) {
            return EMPTY;
        }

        String trimmed = withoutTrailingSlash();

        int lastDelimiter = trimmed.lastIndexOf(PATH_DELIMITER);

        return lastDelimiter == -1 ? EMPTY : trimmed.substring(0, lastDelimiter) + PATH_DELIMITER;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    private boolean computeIsDirectory() {
        return isRoot() ||  value.endsWith(PATH_DELIMITER);
    }

    public boolean isRoot() {
        return value.isEmpty();
    }

    protected String withoutTrailingSlash() {

        return value.endsWith(PATH_DELIMITER)
                ? value.substring(0, value.length() - 1) : value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        BasePath basePath = (BasePath) o;
        return isDirectory == basePath.isDirectory && value.equals(basePath.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + Boolean.hashCode(isDirectory);
        return result;
    }
}
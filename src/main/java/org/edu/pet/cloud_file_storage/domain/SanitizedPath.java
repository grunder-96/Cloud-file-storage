package org.edu.pet.cloud_file_storage.domain;

import org.edu.pet.cloud_file_storage.exception.InvalidPathException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SanitizedPath extends BasePath {

    private static final String TOP_PATH = "..";
    private static final String CURRENT_PATH = ".";

    private static final Pattern FORBIDDEN_CHARS_PATTERN = Pattern.compile("[<>:\"|?*\\x00-\\x1F\\x7F]+");
    private static final Pattern DELIMITERS_PATTERN = Pattern.compile("[/\\\\]+");

    private static final int MAX_SEGMENT_BYTES = 255;

    public static final SanitizedPath ROOT = new SanitizedPath(EMPTY);

    public SanitizedPath(String value) {
        super(sanitize(value));
    }

    private SanitizedPath(String first, String second) {
        super(first + second);
    }

    public SanitizedPath resolve(SanitizedPath path) {

        if (this.isRoot()) {
            return path;
        }

        if (!this.isDirectory()) {
            throw new InvalidPathException("Source path must be a directory");
        }

        if (path.isRoot()) {
            return this;
        }

        return new SanitizedPath(this.value, path.value());
    }

    private static String sanitize(String rawPath) {

        if (rawPath == null) {
            throw new InvalidPathException("The path cannot be null");
        }

        if (rawPath.isBlank()) {
            return EMPTY;
        }

        String sanitizedPath = rawPath.strip();

        sanitizedPath = DELIMITERS_PATTERN.matcher(sanitizedPath)
                .replaceAll(PATH_DELIMITER)
                .replaceFirst("^/+", "");

        boolean isDirectory = sanitizedPath.endsWith(PATH_DELIMITER);

        validatePath(sanitizedPath);

        String joined = Arrays.stream(sanitizedPath.split(PATH_DELIMITER))
                .map(String::strip)
                .filter(Predicate.not(String::isEmpty))
                .map(segment -> {

                    validatePathSegment(segment);
                    return segment;
                })
                .collect(Collectors.joining(PATH_DELIMITER));

        if (joined.isEmpty()) {
            return EMPTY;
        }

        return isDirectory ? joined + PATH_DELIMITER : joined;
    }

    private static void validatePathSegment(String pathSegment) {

        if (pathSegment.equals(TOP_PATH) || pathSegment.equals(CURRENT_PATH)) {
            throw new InvalidPathException("Dot segments .. or . not supported");
        }

        if (pathSegment.getBytes(StandardCharsets.UTF_8).length > MAX_SEGMENT_BYTES) {
            throw new InvalidPathException("Path segment size must not exceed %d bytes".formatted(MAX_SEGMENT_BYTES));
        }
    }

    private static void validatePath(String path) {

        if (FORBIDDEN_CHARS_PATTERN.matcher(path).find()) {
            throw new InvalidPathException("Path contains forbidden characters");
        }
    }
}
package org.edu.pet.cloud_file_storage.service.s3;

import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.springframework.stereotype.Service;

@Service
public class S3PathService {

    private static final String USER_FOLDER_PREFIX_TEMPLATE = "user-%d-files/";

    public S3Key build(SanitizedPath sanitizedPath, long userId) {

        String key = buildUserPrefix(userId) + sanitizedPath.value();
        return new S3Key(key);
    }

    public AnonymousPath anonymize(S3Key key, long userId) {

        if (key == null) {
            throw new InvalidPathException("The path cannot be null");
        }

        String prefix = buildUserPrefix(userId);

        if (!key.value().startsWith(prefix)) {
            throw new InvalidPathException("Path does not belong to user with id " + userId);
        }

        return new AnonymousPath(key.value().substring(prefix.length()));
    }

    private String buildUserPrefix(long userId) {
        return USER_FOLDER_PREFIX_TEMPLATE.formatted(userId);
    }
}
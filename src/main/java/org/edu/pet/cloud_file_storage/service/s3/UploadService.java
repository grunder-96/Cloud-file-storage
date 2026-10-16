package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.domain.BasePath;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceAlreadyExistsException;
import org.edu.pet.cloud_file_storage.exception.S3UploadException;
import org.edu.pet.cloud_file_storage.mapper.ObjectInfoDtoMapper;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3RollbackService;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadService {

    private final S3PathService pathService;
    private final S3Storage storage;

    private final S3RollbackService rollbackService;

    private final ObjectInfoDtoMapper objectInfoMapper;

    @Value("${minio.bucket}")
    private String bucket;

    public List<ObjectInfoDto> upload(List<MultipartFile> files, SanitizedPath uploadPath, long userId) {

        if (files.isEmpty()) {
            return List.of();
        }

        validateUploadPath(uploadPath, userId);

        List<UploadEntry> uploadEntries = buildUploadEntries(files, uploadPath, userId);

        List<ObjectInfoDto> results = new ArrayList<>();
        List<S3Key> createdKeys = new ArrayList<>();

        validateNameCollisions(uploadEntries);

        try {

            for (UploadEntry uploadEntry : uploadEntries) {

                boolean created;

                try (InputStream input = uploadEntry.openStream()) {

                    created = storage.putObjectIfAbsent(bucket, uploadEntry.key(),
                            input, uploadEntry.size(), -1);

                    AnonymousPath anonymousPath = pathService.anonymize(uploadEntry.key(), userId);

                    if (created) {
                        createdKeys.add(uploadEntry.key());
                    } else if (!uploadEntry.isDirectory()) {
                        throw new S3ResourceAlreadyExistsException("File %s already exists".formatted(anonymousPath.value()));
                    }

                    results.add(objectInfoMapper.toObjectInfoDto(anonymousPath, uploadEntry.size()));
                }
            }
        } catch (S3ResourceAlreadyExistsException | InvalidPathException e) {

            rollbackService.rollback(createdKeys, "upload for user %d".formatted(userId));
            throw e;
        } catch (Exception e) {

            rollbackService.rollback(createdKeys, "upload for user %d".formatted(userId));
            throw new S3UploadException("Upload operation for user with id %d failed".formatted(userId), e);
        }

        return results;
    }

    private List<UploadEntry> buildUploadEntries(List<MultipartFile> files, SanitizedPath uploadPath, long userId) {

        Map<S3Key, UploadEntry> entries = new LinkedHashMap<>();

        for (MultipartFile file : files) {

            SanitizedPath filePath = new SanitizedPath(file.getOriginalFilename());

            if (filePath.isDirectory()) {
                throw new InvalidPathException("File name must not be empty or end with a slash");
            }

            toDirectoryEntries(filePath, uploadPath, userId)
                    .forEach(dir -> entries.putIfAbsent(dir.key(), dir));

            S3Key fileKey = buildFullKey(filePath, uploadPath, userId);

            if (entries.putIfAbsent(fileKey, new UploadEntry(fileKey, file)) != null) {
                throw new InvalidPathException("File %s is present in the upload more than once".formatted(filePath.value()));
            }
        }

        validateInternalCollision(entries.keySet());

        return List.copyOf(entries.values());
    }

    private S3Key buildFullKey(SanitizedPath filePath, SanitizedPath uploadPath, long userId) {
        return pathService.build(uploadPath.resolve(filePath), userId);
    }

    private Stream<UploadEntry> toDirectoryEntries(SanitizedPath path, SanitizedPath uploadPath, long userId) {

        return splitIntoDirectories(path).stream()
            .map(p -> new UploadEntry(buildFullKey(p, uploadPath, userId), null));
    }

    private List<SanitizedPath> splitIntoDirectories(SanitizedPath path) {

        if (path.isRoot()) {
            return List.of();
        }

        String pathToSplit = path.isDirectory() ?
                path.value() : path.parent();

        // file in root
        if (pathToSplit.isEmpty()) {
            return List.of();
        }

        String[] segments = pathToSplit.split(BasePath.PATH_DELIMITER);

        StringBuilder sb = new StringBuilder();
        List<SanitizedPath> result = new ArrayList<>();

        for (String segment : segments) {

            sb.append(segment).append(BasePath.PATH_DELIMITER);
            result.add(new SanitizedPath(sb.toString()));
        }

        return result;
    }

    private void validateUploadPath(SanitizedPath path, long userId) {

        if (!path.isDirectory()) {
            throw new InvalidPathException("Upload path is not directory");
        }

        if (!path.isRoot()) {

            S3Key pathKey = pathService.build(path, userId);

            if (!storage.objectExists(bucket, pathKey)) {
                throw new InvalidPathException("Upload directory does not exist");
            }
        }
    }

    private void validateInternalCollision(Set<S3Key> keys) {

        for (S3Key key : keys) {

            S3Key opposite = key.opposite();

            if (keys.contains(opposite)) {

                log.info("Name collision: {} key conflicts with {} key in upload keyset", key.value(), opposite.value());
                throw new InvalidPathException("There cannot be a folder and a file with the same name in one directory");
            }
        }
    }

    private void validateNameCollisions(List<UploadEntry> entries) {

        Map<S3Key, List<S3Key>> keysByParent = entries.stream()
                .map(UploadEntry::key)
                .collect(Collectors.groupingBy(S3Key::parent));

        keysByParent.forEach((parent, keys) -> {

            Set<String> existing = storage.listObjects(bucket, parent, false).stream()
                    .map(S3ObjectItem::objectName)
                    .collect(Collectors.toSet());

            for (S3Key key : keys) {

                S3Key opposite = key.opposite();

                if (existing.contains(opposite.value())) {

                    log.info("Name collision: {} key conflicts with already existing {} key",
                            key.value(), opposite.value());
                    throw new InvalidPathException("There cannot be a folder and a file with the same name in one directory");
                }
            }
        });
    }

    private record UploadEntry(S3Key key, MultipartFile file) {

        UploadEntry {

            if (key.isDirectory() != Objects.isNull(file)) {
                throw new IllegalArgumentException("Directory upload entry must have no file, file upload entry must have one");
            }
        }

        boolean isDirectory() {
            return Objects.isNull(file);
        }

        InputStream openStream() throws IOException {
            return isDirectory() ? new ByteArrayInputStream(new byte[0]) : file.getInputStream();
        }

        long size() {
            return isDirectory() ? 0 : file.getSize();
        }
    }
}
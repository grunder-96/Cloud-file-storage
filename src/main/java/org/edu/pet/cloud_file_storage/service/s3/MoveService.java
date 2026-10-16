package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.dto.s3.S3StatObjectResponse;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3PartialRemoveException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceAlreadyExistsException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.mapper.ObjectInfoDtoMapper;
import org.edu.pet.cloud_file_storage.mapper.S3Mapper;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3RollbackService;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MoveService {

    private final S3PathService pathService;
    private final S3Storage storage;

    private final S3RollbackService rollbackService;
    private final S3ObjectValidator validator;
    private final S3Mapper mapper;
    private final ObjectInfoDtoMapper infoDtoMapper;

    @Value("${minio.bucket}")
    private String bucket;

    public ObjectInfoDto move(SanitizedPath from, SanitizedPath to, long userId) {

        validateRoot(from, to);
        validateCompatibleTypes(from, to);
        validateOneOperationAtTime(from, to);

        S3Key fromKey = pathService.build(from, userId);
        S3Key toKey = pathService.build(to, userId);

        S3StatObjectResponse statObjectResponse;

        try {
            statObjectResponse = storage.getStatObject(bucket, fromKey);
        } catch (S3ResourceNotFoundException e) {
            throw new S3ResourceNotFoundException("Source object not found", e.getCause());
        }

        if (storage.objectExists(bucket, toKey)) {
            throw new S3ResourceAlreadyExistsException("Target object already exists");
        }

        if (!storage.objectExists(bucket, toKey.parent())) {
            throw new S3ResourceNotFoundException("Target parent directory does not exist");
        }

        validator.validateNameCollision(toKey);

        return from.isDirectory() ? moveDirectory(fromKey, toKey, userId) :
                moveFile(fromKey, toKey, statObjectResponse.size(), userId);
    }

    private ObjectInfoDto moveDirectory(S3Key fromKey, S3Key toKey, long userId) {

        if (toKey.value().startsWith(fromKey.value())) {
            throw new InvalidPathException("Cannot move directory into itself");
        }

        List<S3ObjectItem> s3ObjectItems = storage.listObjects(bucket, fromKey, true);

        List<S3Key> keys = s3ObjectItems.stream()
                .map(mapper::toS3Key)
                .toList();

        List<S3Key> updatedKeys = keys.stream()
                .map(key -> toKey.value() + key.value().substring(fromKey.value().length()))
                .map(S3Key::new)
                .toList();

        List<S3Key> copiedKeys = new ArrayList<>();

        try {

            for (int i = 0; i < keys.size(); i++) {

                storage.copy(bucket, keys.get(i), updatedKeys.get(i));
                copiedKeys.add(updatedKeys.get(i));
            }
        } catch (Exception e) {

            rollbackService.rollback(copiedKeys, "move of directory %s -> %s".formatted(fromKey.value(), toKey.value()));
            throw e;
        }

        try {
            storage.remove(bucket, keys);
        } catch (S3PartialRemoveException e) {

            log.error("Move {} -> {}: {} of {} source keys were not removed, copies are kept: {}",
                    fromKey.value(), toKey.value(), e.getFailedKeys().size(), keys.size(),
                    e.getFailedKeys().stream().limit(20).toList());
        } catch (Exception e) {

            log.error("Move {} -> {}: removing {} source keys failed, copies are kept",
                    fromKey.value(), toKey.value(), keys.size(), e);
        }

        AnonymousPath anonymousPath = pathService.anonymize(toKey, userId);
        return infoDtoMapper.toObjectInfoDto(anonymousPath, 0);
    }

    private ObjectInfoDto moveFile(S3Key fromKey, S3Key toKey, long fileSize, long userId) {

        storage.copy(bucket, fromKey, toKey);

        try {
            storage.remove(bucket, fromKey);
        } catch (Exception e) {

            rollbackService.rollback(List.of(toKey), "move of file " + fromKey.value());
            throw e;
        }

        AnonymousPath anonymousPath = pathService.anonymize(toKey, userId);
        return infoDtoMapper.toObjectInfoDto(anonymousPath, fileSize);
    }

    private void validateRoot(SanitizedPath from, SanitizedPath to) {

        if (from.isRoot() || to.isRoot()) {
            throw new InvalidPathException("Root directory cannot be used as source or target");
        }
    }

    private void validateOneOperationAtTime(SanitizedPath from, SanitizedPath to) {

        boolean isRename = !from.name().equals(to.name());
        boolean isMove = !from.parent().equals(to.parent());

        if (isRename == isMove) {
            throw new InvalidPathException("Exactly one operation must be performed: either rename or move");
        }
    }

    private void validateCompatibleTypes(SanitizedPath from, SanitizedPath to) {

        if (from.isDirectory() != to.isDirectory()) {
            throw new InvalidPathException("Paths must be same types");
        }
    }
}
package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceAlreadyExistsException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.mapper.ObjectInfoDtoMapper;
import org.edu.pet.cloud_file_storage.mapper.S3Mapper;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorySpecificService {

    private final S3PathService pathService;
    private final S3Storage storage;

    private final S3ObjectValidator validator;
    private final ObjectInfoDtoMapper objectInfoMapper;
    private final S3Mapper s3Mapper;

    @Value("${minio.bucket}")
    private String bucket;

    public List<ObjectInfoDto> getDirectoryContentList(SanitizedPath sanitizedDirectoryPath, long userId) {

        if (!sanitizedDirectoryPath.isDirectory()) {
            throw new InvalidPathException("Path is not directory");
        }

        S3Key directoryKey = pathService.build(sanitizedDirectoryPath, userId);

        if (!storage.objectExists(bucket, directoryKey)) {
            throw new S3ResourceNotFoundException("Directory not found");
        }

        List<S3ObjectItem> items = storage.listObjects(bucket, directoryKey, false);

        return items.stream()
            .filter(item -> !item.objectName().equals(directoryKey.value()))
            .map(item -> {

                AnonymousPath path = pathService.anonymize(s3Mapper.toS3Key(item), userId);
                return objectInfoMapper.toObjectInfoDto(path, item.size());
            })
            .toList();
    }

    public ObjectInfoDto createDirectory(SanitizedPath path, long userId) {

        if (!path.isDirectory()) {
            throw new InvalidPathException("Path is not directory");
        }

        if (!path.isRoot()) {

            S3Key parentKey = pathService.build(new SanitizedPath(path.parent()), userId);

            if (!storage.objectExists(bucket, parentKey)) {
                throw new S3ResourceNotFoundException("Parent directory does not exist");
            }
        }

        S3Key key = pathService.build(path, userId);

        validator.validateNameCollision(key);

        if (!storage.putObjectIfAbsent(bucket, key, new ByteArrayInputStream(new byte[]{}), 0, -1)) {
            throw new S3ResourceAlreadyExistsException("Directory already exists");
        }

        AnonymousPath anonymousPath = pathService.anonymize(key, userId);
        return objectInfoMapper.toObjectInfoDto(anonymousPath, 0);
    }
}
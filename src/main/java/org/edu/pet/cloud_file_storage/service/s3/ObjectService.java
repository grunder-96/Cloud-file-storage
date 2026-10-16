package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.UserQueryDto;
import org.edu.pet.cloud_file_storage.dto.s3.S3StatObjectResponse;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.mapper.ObjectInfoDtoMapper;
import org.edu.pet.cloud_file_storage.mapper.S3Mapper;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ObjectService {

    private final S3PathService pathService;
    private final S3Storage storage;

    private final S3Mapper mapper;
    private final ObjectInfoDtoMapper objectInfoMapper;

    @Value("${minio.bucket}")
    private String bucket;

    public ObjectInfoDto getObjectInfo(SanitizedPath sanitizedPath, long userId) {

        S3Key key = pathService.build(sanitizedPath, userId);

        S3StatObjectResponse statObject = storage.getStatObject(bucket, key);

        AnonymousPath anonymousPath = pathService.anonymize(key, userId);

        return objectInfoMapper.toObjectInfoDto(anonymousPath, statObject.size());
    }

    public void remove(SanitizedPath path, long userId) {

        if (path.isRoot()) {
            throw new InvalidPathException("Root directory cannot be deleted");
        }

        S3Key key = pathService.build(path, userId);

        if (!storage.objectExists(bucket, key)) {
            throw new S3ResourceNotFoundException("Resource not found");
        }

        if (path.isDirectory()) {

            List<S3Key> keys = storage.listObjects(bucket, key, true).stream()
                    .map(mapper::toS3Key)
                    .toList();

            storage.remove(bucket, keys);
            return;
        }

        storage.remove(bucket, key);
    }

    public List<ObjectInfoDto> search(UserQueryDto queryDto, long userId) {

        S3Key rootKey = pathService.build(SanitizedPath.ROOT, userId);
        String query = queryDto.query().toLowerCase();

        return storage.listObjects(bucket, rootKey, true).stream()
                .map(item -> {
                    S3Key key = mapper.toS3Key(item);
                    AnonymousPath path = pathService.anonymize(key, userId);
                    return Map.entry(path, item.size());
                })
                .filter(entry -> !entry.getKey().isRoot())
                .filter(entry -> entry.getKey().name().toLowerCase().contains(query))
                .map(entry -> objectInfoMapper.toObjectInfoDto(entry.getKey(), entry.getValue()))
                .toList();
    }
}
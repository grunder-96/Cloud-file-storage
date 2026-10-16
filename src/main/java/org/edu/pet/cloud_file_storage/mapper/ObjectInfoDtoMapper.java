package org.edu.pet.cloud_file_storage.mapper;

import org.edu.pet.cloud_file_storage.domain.AnonymousPath;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.ObjectType;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.springframework.stereotype.Component;

@Component
public class ObjectInfoDtoMapper {

    public ObjectInfoDto toObjectInfoDto(AnonymousPath path, long size) {

        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }

        if (size < 0) {
            throw new InvalidPathException("Size must be non-negative");
        }

        validateDirectorySize(path, size);

        return new ObjectInfoDto(
            path.parent(),
            path.name(),
            path.isDirectory() ? null : size,
            path.isDirectory() ? ObjectType.DIRECTORY : ObjectType.FILE
        );
    }

    private void validateDirectorySize(AnonymousPath path, long size) {

        if (path.isDirectory() && size > 0) {
            throw new InvalidPathException("Directory size must be zero");
        }
    }
}
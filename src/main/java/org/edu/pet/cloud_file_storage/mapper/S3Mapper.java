package org.edu.pet.cloud_file_storage.mapper;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.dto.s3.S3StatObjectResponse;
import org.springframework.stereotype.Component;

@Component
public final class S3Mapper {

    public S3StatObjectResponse toS3StatObjectResponse(StatObjectResponse statObjectResponse) {

        if (statObjectResponse == null) {
            return null;
        }

        return new S3StatObjectResponse(
            statObjectResponse.etag(),
            statObjectResponse.size(),
            statObjectResponse.lastModified(),
            statObjectResponse.userMetadata()
        );
    }

    public S3ObjectItem toS3ObjectItem(Item item) {

        if (item == null) {
            return null;
        }

        return new S3ObjectItem(
            item.objectName(),
            item.isDir(),
            item.size()
        );
    }

    public S3Key toS3Key(S3ObjectItem item) {

        if (item == null) {
            return null;
        }

        return new S3Key(item.objectName());
    }
}
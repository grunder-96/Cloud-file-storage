package org.edu.pet.cloud_file_storage.service.s3.storage;

import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.dto.s3.S3StatObjectResponse;

import java.io.InputStream;
import java.util.List;

public interface S3Storage {

    boolean objectExists(String bucket, S3Key key);

    boolean bucketExists(String name);

    void createBucket(String name);

    boolean putObjectIfAbsent(String bucket, S3Key key, InputStream inputStream, long objectSize, long partSize);

    S3StatObjectResponse getStatObject(String bucket, S3Key key);

    List<S3ObjectItem> listObjects(String bucket, S3Key directory, boolean recursive);

    void remove(String bucket, S3Key key);

    void remove(String bucket, List<S3Key> keys);

    void copy(String bucket, S3Key srcKey, S3Key destKey);

    InputStream getObject(String bucket, S3Key key);
}
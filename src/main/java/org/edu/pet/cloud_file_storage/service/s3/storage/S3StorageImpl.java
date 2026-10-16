package org.edu.pet.cloud_file_storage.service.s3.storage;

import io.minio.BucketExistsArgs;
import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.dto.s3.S3StatObjectResponse;
import org.edu.pet.cloud_file_storage.exception.S3PartialRemoveException;
import org.edu.pet.cloud_file_storage.exception.S3PreconditionFailedException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.mapper.S3Mapper;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class S3StorageImpl implements S3Storage {

    private final MinioClient minioClient;

    private final S3Mapper mapper;

    @Override
    public boolean bucketExists(String name) {

        return S3Executor.execute(

                client -> client.bucketExists(BucketExistsArgs.builder()
                        .bucket(name)
                        .build()),
                minioClient
        );
    }

    @Override
    public void createBucket(String name) {

        S3Executor.execute(

                client -> {

                    client.makeBucket(MakeBucketArgs.builder()
                            .bucket(name)
                            .build());

                    return null;
                },
                minioClient
        );
    }

    @Override
    public S3StatObjectResponse getStatObject(String bucket, S3Key key) {

        return S3Executor.execute(

                client -> {

                    StatObjectResponse statObjectResponse = client.statObject(StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(key.value())
                            .build());

                    return mapper.toS3StatObjectResponse(statObjectResponse);
                },
                minioClient
        );
    }

    @Override
    public List<S3ObjectItem> listObjects(String bucket, S3Key directory, boolean recursive) {

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Listing requires a directory key: " + directory.value());
        }

        return S3Executor.execute(

                client -> {

                    Iterable<Result<Item>> results = client.listObjects(ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(directory.value())
                            .recursive(recursive)
                            .build());

                    List<S3ObjectItem> items = new ArrayList<>();

                    for (Result<Item> result : results) {

                        Item item = result.get();
                        items.add(mapper.toS3ObjectItem(item));
                    }

                    return items;
                },
                minioClient
        );
    }

    @Override
    public boolean objectExists(String bucket, S3Key key) {

        try {

            return S3Executor.execute(

                    client -> {

                        client.statObject(StatObjectArgs.builder()
                                .bucket(bucket)
                                .object(key.value())
                                .build());

                        return true;
                    },
                    minioClient);

        } catch (S3ResourceNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean putObjectIfAbsent(String bucket, S3Key key, InputStream inputStream,
                                     long objectSize, long partSize) {

        try {

            S3Executor.execute(

                    client -> {

                        client.putObject(PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(key.value())
                                .headers(Map.of("If-None-Match", "*"))
                                .stream(inputStream, objectSize, partSize)
                                .build());

                        return null;
                    },
                    minioClient
            );

            return true;
        } catch (S3PreconditionFailedException e) {
            return false;
        }
    }

    @Override
    public void remove(String bucket, List<S3Key> keys) {

        List<DeleteObject> objects = keys.stream()
                .map(key -> new DeleteObject(key.value()))
                .toList();

        List<String> failedKeys = S3Executor.execute(

                client -> {

                    List<String> failures = new ArrayList<>();

                    Iterable<Result<DeleteError>> results = client.removeObjects(RemoveObjectsArgs.builder()
                            .bucket(bucket)
                            .objects(objects)
                            .build());

                    for (Result<DeleteError> result : results) {

                        DeleteError error = result.get();

                        if (error != null) {
                            failures.add(error.objectName());
                        }
                    }

                    return failures;
                },
                minioClient
        );

        if (!failedKeys.isEmpty()) {
            throw new S3PartialRemoveException(failedKeys);
        }
    }

    @Override
    public void remove(String bucket, S3Key key) {

        S3Executor.execute(

                client -> {

                    client.removeObject(RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key.value())
                            .build());

                    return null;
                },
                minioClient
        );
    }

    @Override
    public void copy(String bucket, S3Key srcKey, S3Key destKey) {

        S3Executor.execute(

                client -> {

                    client.copyObject(CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(destKey.value())
                            .source(CopySource.builder()
                                    .bucket(bucket)
                                    .object(srcKey.value())
                                    .build())
                            .build());

                    return null;
                },
                minioClient
        );
    }

    @Override
    public InputStream getObject(String bucket, S3Key key) {

        return S3Executor.execute(

                client -> client.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(key.value())
                        .build()),
                minioClient
        );
    }
}
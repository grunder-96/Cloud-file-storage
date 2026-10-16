package org.edu.pet.cloud_file_storage.service.s3.storage;

import io.minio.MinioClient;
import io.minio.errors.MinioException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@FunctionalInterface
public interface S3Execution<T> {

    T execute(MinioClient minioClient) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException;
}

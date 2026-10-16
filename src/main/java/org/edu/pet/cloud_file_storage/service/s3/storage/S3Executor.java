package org.edu.pet.cloud_file_storage.service.s3.storage;

import io.minio.MinioClient;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.ErrorResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.edu.pet.cloud_file_storage.exception.S3ExecutionException;
import org.edu.pet.cloud_file_storage.exception.S3PreconditionFailedException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class S3Executor {

    private static final String PRECONDITION_FAILED = "PreconditionFailed";
    private static final String NO_KEY = "NoSuchKey";

    public static <T> T execute(S3Execution<T> execution, MinioClient minioClient) {

        try {
            return execution.execute(minioClient);
        } catch (ErrorResponseException e) {

            if (isNoSuchKey(e)) {
                throw new S3ResourceNotFoundException("The specified resource was not found", e);
            }

            if (isPreconditionFailed(e)) {
                throw new S3PreconditionFailedException("One or more preconditions failed", e);
            }

            throw new S3ExecutionException(resolveMessage(e), e);
        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new S3ExecutionException(e.getMessage(), e);
        }
    }

    private static boolean isPreconditionFailed(ErrorResponseException e) {
        return isSpecificErrorCode(e, PRECONDITION_FAILED);
    }

    private static boolean isNoSuchKey(ErrorResponseException e) {
        return isSpecificErrorCode(e, NO_KEY);
    }

    private static boolean isSpecificErrorCode(ErrorResponseException e, String code) {

        ErrorResponse er = e.errorResponse();
        return er != null && code.equals(er.code());
    }

    private static String resolveMessage(ErrorResponseException e) {

        ErrorResponse er = e.errorResponse();

        if (er == null || er.message() == null) {
            return e.getMessage();
        }

        return er.message();
    }
}
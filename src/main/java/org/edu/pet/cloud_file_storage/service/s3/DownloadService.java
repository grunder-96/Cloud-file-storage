package org.edu.pet.cloud_file_storage.service.s3;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.S3Key;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.s3.S3ObjectItem;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.mapper.S3Mapper;
import org.edu.pet.cloud_file_storage.service.s3.storage.S3Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class DownloadService {

    private final S3PathService pathService;
    private final S3Storage storage;

    private final S3Mapper mapper;

    @Value("${minio.bucket}")
    private String bucket;

    private static final String ARCHIVE_EXTENSION = ".zip";

    public void validateDownloadPath(SanitizedPath path, long userId) {

        if (path.isRoot()) {
            throw new InvalidPathException("Path must not be empty");
        }

        S3Key key = pathService.build(path, userId);

        if (!storage.objectExists(bucket, key)) {
            throw new S3ResourceNotFoundException("Resource not found");
        }
    }

    public void download(SanitizedPath path, long userId, OutputStream output) throws IOException {

        S3Key key = pathService.build(path, userId);

        if (path.isDirectory()) {

            ZipOutputStream zip = new ZipOutputStream(output);
            writeDirectoryZip(key, path.name(), zip);
            zip.finish();

            return;
        }

        try (InputStream input = storage.getObject(bucket, key)) {
            input.transferTo(output);
        }
    }

    private void writeDirectoryZip(S3Key directoryKey, String directoryName, ZipOutputStream output) throws IOException {

        List<S3ObjectItem> items = storage.listObjects(bucket, directoryKey, true);

        if (items.size() == 1 && items.getFirst().objectName().equals(directoryKey.value())) {

            output.putNextEntry(new ZipEntry(directoryName));
            output.closeEntry();
            return;
        }

        for (S3ObjectItem item : items) {

            S3Key s3Key = mapper.toS3Key(item);
            String entryName = s3Key.value().substring(directoryKey.value().length());

            if (entryName.isEmpty()) {
                continue;
            }

            output.putNextEntry(new ZipEntry(entryName));

            if (!s3Key.isDirectory()) {

                try (InputStream input = storage.getObject(bucket, s3Key)) {
                    input.transferTo(output);
                }
            }

            output.closeEntry();
        }
    }

    public String getDownloadName(SanitizedPath path) {

        if (path.isDirectory()) {

            String directoryName = path.name();
            String withoutSlash = directoryName.substring(0, directoryName.length() - 1);
            return withoutSlash + ARCHIVE_EXTENSION;
        }

        return path.name();
    }
}
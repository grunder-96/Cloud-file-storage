package org.edu.pet.cloud_file_storage.dto.s3;

import java.time.ZonedDateTime;
import java.util.Map;

public record S3StatObjectResponse(String etag,
                                   long size,
                                   ZonedDateTime lastModified,
                                   Map<String, String> userMetadata) {

}
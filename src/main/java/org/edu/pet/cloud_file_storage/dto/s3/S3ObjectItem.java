package org.edu.pet.cloud_file_storage.dto.s3;

public record S3ObjectItem(String objectName, boolean isDirectory, long size) {

}
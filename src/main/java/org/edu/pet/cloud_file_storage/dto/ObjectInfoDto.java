package org.edu.pet.cloud_file_storage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metadata of a file or directory")
public record ObjectInfoDto(
        @Schema(
                description = "Path to the folder containing the resource",
                example = "folder1/folder2/"
        )
        String path,
        @Schema(
                description = "Name of the resource",
                example = "file.txt"
        )
        String name,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(
                description = "Size of the file in bytes. Absent for directories",
                example = "123"
        )
        Long size,
        @Schema(
                description = "Type of the resource",
                example = "FILE"
        )
        ObjectType type) {

}
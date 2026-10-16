package org.edu.pet.cloud_file_storage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response")
public record ErrorResponseDto<T>(

        @Schema(description = "Human-readable error message")
        T message) {
}
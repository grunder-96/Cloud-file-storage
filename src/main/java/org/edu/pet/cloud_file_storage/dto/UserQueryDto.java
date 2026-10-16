package org.edu.pet.cloud_file_storage.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserQueryDto(

        @Parameter(description = "Search query", required = true, example = "report")
        @NotNull(message = "cannot be null")
        @NotBlank(message = "cannot be blank")
        @Length(min = 2, max = 50, message = "must be size between 2 and 50 characters long")
        String query) {

}
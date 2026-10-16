package org.edu.pet.cloud_file_storage.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignInRequestDto(

        @NotNull(message = "cannot be null")
        @NotBlank(message = "cannot be blank")
        @Size(min = 5, max = 20, message = "should be {min}-{max} characters long")
        @Pattern(regexp = "^[a-zA-Z][\\w-]+[a-zA-Z0-9]$",
            message = "must start with a letter and end with a letter or digit, contain only a-zA-Z0-9_-")
        String username,

        @NotNull(message = "cannot be null")
        @NotBlank(message = "cannot be blank")
        @Size(min = 6, max = 15, message = "should be {min}-{max} characters long")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=_!-]).*$",
            message = "must contain at least one capital and lowercase letter, one digit and one special symbol")
        String password) {

}
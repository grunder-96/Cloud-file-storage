package org.edu.pet.cloud_file_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotBlank(message = "cannot be blank")
    @Length(min = 5, max = 20, message = "should be {min}-{max} characters long")
    @Pattern(regexp = "^[a-zA-Z][\\w-]+[a-zA-Z0-9]$",
            message = "must start with a letter and end with a letter or digit, contain only a-zA-Z0-9_-")
    private String username;
    @NotBlank(message = "cannot be blank")
    @Length(min = 6, max = 15, message = "should be {min}-{max} characters long")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=_!-]).*$",
            message = "must contain at least one capital and lowercase letter, one digit and one special symbol")
    private String password;
}
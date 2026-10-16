package org.edu.pet.cloud_file_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.ErrorResponseDto;
import org.edu.pet.cloud_file_storage.dto.auth.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.service.auth.LoginService;
import org.edu.pet.cloud_file_storage.service.auth.RegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@SecurityRequirements
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Auth-related operations")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    @PostMapping("/sign-up")
    @Operation(
            summary = "Registration",
            description = "Creates a new user and returns their username",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User's login and password", required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SignUpRequestDto.class),
                            examples =  {
                                @ExampleObject(name = "Valid credentials", value = "{ \"username\": \"user_1\", \"password\": \"Qwe12345!\" }"),
                                @ExampleObject(name = "Invalid credentials", description = "The login is too short and the password is too simple", value = "{ \"username\": \"usr\", \"password\": \"Pass\" }")
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Success attempt registration", content =
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDto.class)
                            )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation error", content =
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
            ),
            @ApiResponse(
                    responseCode = "409", description = "Username is taken", content =
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error", content =
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponseDto.class)
                            )
            )
        }
    )
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
                                                    HttpServletRequest req,
                                                    HttpServletResponse resp,
                                                    Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registerService.register(signUpRequestDto, req, resp, auth));
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Login",
            description = "Authenticates user and returns their username",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User's login and password", required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignInRequestDto.class),
                            examples = {
                                    @ExampleObject(name = "Valid credentials", value = "{ \"username\": \"user_1\", \"password\": \"Qwe12345!\" }"),
                                    @ExampleObject(name = "Invalid credentials", description = "Wrong username or password", value = "{ \"username\": \"user_1\", \"password\": \"WrongPass1!\" }")
                            }
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Successful authentication",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400", description = "Validation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401", description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
        }
    )
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody SignInRequestDto signInRequestDto,
                                                 HttpServletRequest req,
                                                 HttpServletResponse resp) {
        return ResponseEntity
                .ok(loginService.login(signInRequestDto, req, resp));
    }

    @PostMapping("/sign-out")
    @SecurityRequirement(name = "sessionAuth")
    @Operation(
            summary = "Logout",
            description = "Invalidates the current session. Handled by Spring Security filters; " +
                    "this controller method is a stub for documentation purposes only"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successful logout"),
            @ApiResponse(
                    responseCode = "401", description = "Not authenticated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
        }
    )
    public void logout() {
        throw new IllegalStateException("This method is implemented by Spring Security filters " +
                "and should never be called directly");
    }
}
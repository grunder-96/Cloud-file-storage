package org.edu.pet.cloud_file_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ErrorResponseDto;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.security.CustomUserDetails;
import org.edu.pet.cloud_file_storage.service.s3.DirectorySpecificService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/directory")
@Tag(name = "Directory operations", description = "Directory-specific operations")
@SecurityRequirement(name = "sessionAuth")
public class DirectoryController {

    private final DirectorySpecificService directoryService;

    @GetMapping
    @Operation(
            summary = "Get directory content",
            description = "Returns a non-recursive list of resources in the given directory",
            parameters = @Parameter(name = "path", description = "Directory path, URL-encoded. Must end with '/'.",
                required = true, example = "folder1/folder2/")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Directory content",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ObjectInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid or missing path",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Directory not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<List<ObjectInfoDto>> getDirectoryContent(@RequestParam("path") SanitizedPath sanitizedPath,
                                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails) {
            return ResponseEntity.ok(directoryService.getDirectoryContentList(sanitizedPath, customUserDetails.getId()));
    }

    @PostMapping
    @Operation(
            summary = "Create directory",
            description = "Creates an empty directory at the given path",
            parameters = @Parameter(name = "path", description = "New directory path, URL-encoded. Must end with '/'.",
                required = true, example = "folder1/folder2/")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201", description = "Directory created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ObjectInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid or missing path",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "404", description = "Parent directory not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Directory already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<ObjectInfoDto> createDirectory(@RequestParam("path") SanitizedPath sanitizedPath,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(directoryService.createDirectory(sanitizedPath, customUserDetails.getId()));
    }
}
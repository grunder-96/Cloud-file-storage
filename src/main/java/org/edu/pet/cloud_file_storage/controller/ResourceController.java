package org.edu.pet.cloud_file_storage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.domain.SanitizedPath;
import org.edu.pet.cloud_file_storage.dto.ErrorResponseDto;
import org.edu.pet.cloud_file_storage.dto.ObjectInfoDto;
import org.edu.pet.cloud_file_storage.dto.UserQueryDto;
import org.edu.pet.cloud_file_storage.security.CustomUserDetails;
import org.edu.pet.cloud_file_storage.service.s3.DownloadService;
import org.edu.pet.cloud_file_storage.service.s3.MoveService;
import org.edu.pet.cloud_file_storage.service.s3.ObjectService;
import org.edu.pet.cloud_file_storage.service.s3.UploadService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Tag(name = "Resource operations", description = "File and directory operations")
@SecurityRequirement(name = "sessionAuth")
public class ResourceController {

    private final ObjectService objectService;
    private final DownloadService downloadService;
    private final UploadService uploadService;
    private final MoveService moveService;

    @GetMapping
    @Operation(
            summary = "Get resource info",
            description = "Returns metadata for a file or directory by its path.",
            parameters = @Parameter(name = "path", description = "Path to resource, URL-encoded. Directory paths must end with '/'.",
                required = true, example = "folder1/folder2/file.txt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resource found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ObjectInfoDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or missing path",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<ObjectInfoDto> getResourceInfo(@RequestParam("path") SanitizedPath sanitizedPath,
                                                         @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(objectService.getObjectInfo(sanitizedPath, customUserDetails.getId()));
    }

    @PostMapping
    @Operation(
        summary = "Upload resources",
        description = "Uploads one or more files. Supports subdirectories in file names",
        parameters = @Parameter(name = "path", description = "Target directory path, URL-encoded. Must end with '/'.",
            example = "folder1/folder2/")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Resources uploaded",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ObjectInfoDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "409", description = "File already exists",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "413", description = "Upload size limit exceeded",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<List<ObjectInfoDto>> upload(@RequestParam("object") @Parameter(description = "Files to upload", required = true) List<MultipartFile> files,
                                                      @RequestParam("path") SanitizedPath sanitizedPath,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        List<ObjectInfoDto> upload = uploadService.upload(files, sanitizedPath, customUserDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(upload);
    }

    @DeleteMapping
    @Operation(
            summary = "Delete resource",
            description = "Deletes a file or directory by its path",
            parameters = @Parameter(name = "path", description = "Path to resource, URL-encoded. Directory paths must end with '/'.",
                example = "folder1/file.txt")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Resource deleted"),
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
                    responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<Void> remove(@RequestParam("path") SanitizedPath sanitizedPath,
                                       @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        objectService.remove(sanitizedPath, customUserDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/move")
    @Operation(
            summary = "Move or rename resource",
            description = "Moves or renames a file or directory",
            parameters = {
                @Parameter(name = "from", description = "Source path, URL-encoded", required = true,
                        example = "folder1/file.txt"),
                @Parameter(name = "to", description = "Destination path(full), URL-encoded", required = true,
                        example = "folder2/file.txt")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Resource moved or renamed",
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
                    responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "409", description = "Resource already exists at target path",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<ObjectInfoDto> move(@RequestParam("from") SanitizedPath from,
                                              @RequestParam("to") SanitizedPath to,
                                              @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(moveService.move(from, to, customUserDetails.getId()));
    }

    @GetMapping("/download")
    @Operation(
            summary = "Download resource",
            description = "Downloads a file as binary. Directories are downloaded as a zip archive",
            parameters = @Parameter(name = "path", description = "Path to resource, URL-encoded. Directory paths must end with '/'.")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Binary content",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
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
                    responseCode = "404", description = "Resource not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<StreamingResponseBody> download(@RequestParam("path") SanitizedPath sanitizedPath,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        downloadService.validateDownloadPath(sanitizedPath, customUserDetails.getId());

        StreamingResponseBody body = out -> {
            downloadService.download(sanitizedPath, customUserDetails.getId(), out);
        };

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(downloadService.getDownloadName(sanitizedPath), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(body);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search resources",
            description = "Searches resources by query string"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Search results",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ObjectInfoDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid or missing query",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401", description = "Not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    public ResponseEntity<List<ObjectInfoDto>> search(@Valid @ParameterObject UserQueryDto queryDto,
                                                      @AuthenticationPrincipal CustomUserDetails customUserDetails) {

        return ResponseEntity.ok(objectService.search(queryDto, customUserDetails.getId()));
    }
}
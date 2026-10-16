package org.edu.pet.cloud_file_storage.advice;

import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.dto.ErrorResponseDto;
import org.edu.pet.cloud_file_storage.exception.InvalidPathException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceAlreadyExistsException;
import org.edu.pet.cloud_file_storage.exception.S3ResourceNotFoundException;
import org.edu.pet.cloud_file_storage.exception.UsernameAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto<String> handleUsernameAlreadyTakenException(UsernameAlreadyTakenException e) {
        return new ErrorResponseDto<>(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        Map<String, String> errors = e.getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                            FieldError::getDefaultMessage,
                            Collectors.joining(", "))
                ));

        String message = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        return new ErrorResponseDto<>(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {

        InvalidPathException invalidPathException = findCause(e, InvalidPathException.class);

        return invalidPathException == null ?
                new ErrorResponseDto<>("The '%s' parameter is invalid".formatted(e.getName())) : new ErrorResponseDto<>(invalidPathException.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto<String> handleBadCredentialsException(BadCredentialsException e) {
        return new ErrorResponseDto<>("There is no such user or the password is incorrect");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        return new ErrorResponseDto<>("The '%s' parameter is required".formatted(e.getParameterName()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleMissingServletRequestPartException(MissingServletRequestPartException e) {
        return new ErrorResponseDto<>("The '%s' part is required".formatted(e.getRequestPartName()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.CONTENT_TOO_LARGE)
    public ErrorResponseDto<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return new ErrorResponseDto<>("Upload size limit exceeded");
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleMultipartException(MultipartException e) {

        log.warn("Multipart request rejected: {}", e.getMessage());
        return new ErrorResponseDto<>("Invalid multipart request");
    }

    @ExceptionHandler(InvalidPathException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseDto<String> handleInvalidPathException(InvalidPathException e) {
        return new ErrorResponseDto<>(e.getMessage());
    }

    @ExceptionHandler(S3ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDto<String> handleS3ResourceNotFoundException(S3ResourceNotFoundException e) {
        return new ErrorResponseDto<>(e.getMessage());
    }

    @ExceptionHandler(S3ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDto<String> handleS3ResourceAlreadyExistsException(S3ResourceAlreadyExistsException e) {
        return new ErrorResponseDto<>(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto<String> handleOtherExceptions(Exception e) {

        log.error(e.getMessage(), e);
        return new ErrorResponseDto<>("Unknown error");
    }

    private <T extends Throwable> T findCause(Throwable throwable, Class<T> clazz) {

        Throwable current = throwable;

        while (current != null) {

            if (clazz.isInstance(current)) {
                return clazz.cast(current);
            }

            current = current.getCause();
        }

        return null;
    }
}
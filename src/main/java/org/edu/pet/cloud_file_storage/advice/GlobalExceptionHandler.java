package org.edu.pet.cloud_file_storage.advice;

import lombok.extern.slf4j.Slf4j;
import org.edu.pet.cloud_file_storage.dto.ErrorResponseDto;
import org.edu.pet.cloud_file_storage.exception.UsernameAlreadyTakenException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
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
    public ErrorResponseDto<Map<String, List<String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        return new ErrorResponseDto<>(
            e.getFieldErrors()
                    .stream()
                    .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(
                            FieldError::getDefaultMessage,
                            Collectors.toList()
                        )
                    ))
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseDto<String> handleBadCredentialsException(BadCredentialsException e) {
        return new ErrorResponseDto<>("There is no such user or the password is incorrect");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDto<String> handleOtherExceptions(RuntimeException e) {
        return new ErrorResponseDto<>("Unknown error");
    }
}
package org.edu.pet.cloud_file_storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.service.LoginService;
import org.edu.pet.cloud_file_storage.service.RegisterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody SignUpRequestDto signUpRequestDto,
                                                    HttpServletRequest req,
                                                    HttpServletResponse resp,
                                                    Authentication auth) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(registerService.register(signUpRequestDto, req, resp, auth));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody SignInRequestDto signInRequestDto,
                                                 HttpServletRequest req,
                                                 HttpServletResponse resp) {
        return ResponseEntity
                .ok(loginService.login(signInRequestDto, req, resp));
    }
}
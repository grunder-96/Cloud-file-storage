package org.edu.pet.cloud_file_storage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserService userService;
    private final LoginService loginService;

    private final ModelMapper modelMapper;

    @Transactional
    public AuthResponseDto register(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest req,
                                    HttpServletResponse resp,
                                    Authentication auth) {

        userService.create(signUpRequestDto);

        if (isAutoLoginRequired(auth)) {

            SignInRequestDto signInRequestDto = modelMapper.map(signUpRequestDto, SignInRequestDto.class);
            return loginService.login(signInRequestDto, req, resp);
        }

        return modelMapper.map(signUpRequestDto, AuthResponseDto.class);
    }

    private boolean isAutoLoginRequired(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }
}
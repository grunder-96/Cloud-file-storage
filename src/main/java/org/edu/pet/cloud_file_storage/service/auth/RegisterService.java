package org.edu.pet.cloud_file_storage.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.auth.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.dto.auth.UserResponseDto;
import org.edu.pet.cloud_file_storage.event.UserRegisteredEvent;
import org.edu.pet.cloud_file_storage.mapper.AuthMapper;
import org.edu.pet.cloud_file_storage.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserService userService;

    private final LoginService loginService;

    private final ApplicationEventPublisher eventPublisher;

    private final AuthMapper mapper;

    @Transactional
    public AuthResponseDto register(SignUpRequestDto signUpRequestDto,
                                    HttpServletRequest req,
                                    HttpServletResponse resp,
                                    Authentication auth) {

        UserResponseDto userResponseDto = userService.create(signUpRequestDto);

        eventPublisher.publishEvent(new UserRegisteredEvent(userResponseDto.id()));

        if (isAutoLoginRequired(auth)) {

            SignInRequestDto signInRequestDto = new SignInRequestDto(signUpRequestDto.username(),
                    signUpRequestDto.password());

            return loginService.login(signInRequestDto, req, resp);
        }

        return mapper.toAuthResponse(signUpRequestDto);
    }

    private boolean isAutoLoginRequired(Authentication auth) {
        return auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken;
    }
}
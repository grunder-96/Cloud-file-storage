package org.edu.pet.cloud_file_storage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final UserService userService;
    private final LoginService loginService;

    private final ModelMapper modelMapper;

    @Transactional
    public AuthResponseDto register(SignUpRequestDto signUpRequestDto, HttpServletRequest req, HttpServletResponse resp) {

        userService.create(signUpRequestDto);
        SignInRequestDto signInRequestDto = modelMapper.map(signUpRequestDto, SignInRequestDto.class);

        return loginService.login(signInRequestDto, req, resp);
    }
}
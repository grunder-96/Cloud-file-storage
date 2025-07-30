package org.edu.pet.cloud_file_storage.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.SignInRequestDto;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final ModelMapper modelMapper;

    private final AuthenticationManager authManager;
    private final SecurityContextService securityContextService;

    public AuthResponseDto login(SignInRequestDto signInRequestDto, HttpServletRequest req, HttpServletResponse resp) {

        var authToken = UsernamePasswordAuthenticationToken
                .unauthenticated(signInRequestDto.getUsername(), signInRequestDto.getPassword());

        Authentication authResponse = authManager.authenticate(authToken);

        securityContextService.injectSecurityContext(authResponse, req, resp);

        return modelMapper.map(signInRequestDto, AuthResponseDto.class);
    }
}
package org.edu.pet.cloud_file_storage.mapper;

import org.edu.pet.cloud_file_storage.dto.auth.AuthResponseDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignInRequestDto;
import org.edu.pet.cloud_file_storage.dto.auth.SignUpRequestDto;
import org.mapstruct.Mapper;
import org.springframework.security.core.userdetails.UserDetails;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface AuthMapper {

    AuthResponseDto toAuthResponse(SignUpRequestDto signUpRequestDto);

    AuthResponseDto toAuthResponse(SignInRequestDto signInRequestDto);

    AuthResponseDto toAuthResponse(UserDetails userDetails);
}
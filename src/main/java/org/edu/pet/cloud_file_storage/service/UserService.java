package org.edu.pet.cloud_file_storage.service;

import lombok.RequiredArgsConstructor;
import org.edu.pet.cloud_file_storage.dto.SignUpRequestDto;
import org.edu.pet.cloud_file_storage.dto.UserResponseDto;
import org.edu.pet.cloud_file_storage.entity.UserEntity;
import org.edu.pet.cloud_file_storage.exception.UsernameAlreadyTakenException;
import org.edu.pet.cloud_file_storage.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    
    private static final String USERNAME_UNIQUE_INDEX_NAME = "idx_users_username_lower";

    @Transactional
    public UserResponseDto create(SignUpRequestDto signUpRequestDto) {

        UserEntity savedUser = modelMapper.map(signUpRequestDto, UserEntity.class);

        try {
            userRepository.save(savedUser);
        } catch (DataIntegrityViolationException e) {
            handleDataIntegrityViolationException(e);
        }

        return modelMapper.map(savedUser, UserResponseDto.class);
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> findByUsernameIgnoreCase(String username) {
        return  userRepository.findByUsernameIgnoreCase(username);
    }

    private void handleDataIntegrityViolationException(DataIntegrityViolationException e) {

        Throwable cause = e.getCause();

        if (cause instanceof ConstraintViolationException) {
            if (cause.getMessage().contains(USERNAME_UNIQUE_INDEX_NAME)) {
                throw new UsernameAlreadyTakenException("Username is already taken", e);
            }
        }

        throw e;
    }
}